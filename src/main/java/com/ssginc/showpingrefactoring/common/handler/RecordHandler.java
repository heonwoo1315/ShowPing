package com.ssginc.showpingrefactoring.common.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ssginc.showpinglive.util.UserRegistry;
import com.ssginc.showpinglive.util.UserSession;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * @author dckat
 * 녹화 관련 웹소켓 메시지 처리 핸들러 클래스
 * <p>
 */
public class RecordHandler extends TextWebSocketHandler {

    @Value("${upload.path}")
    private String RECORDER_FILE_PATH;

    private final Logger log = LoggerFactory.getLogger(RecordHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private UserRegistry registry;

    @Autowired
    private KurentoClient kurento;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        log.debug("Incoming message: {}", jsonMessage);

        UserSession user = registry.getBySession(session);
        if (user != null) {
            log.debug("Incoming message from user '{}': {}", user.getId(), jsonMessage);
        } else {
            log.debug("Incoming message from new user: {}", jsonMessage);
        }

        switch (jsonMessage.get("id").getAsString()) {
            case "start":
                record(session, jsonMessage);
                break;
            case "stop":
                if (user != null) {
                    user.stop();
                }
                break;
            case "onIceCandidate": {
                JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();

                if (user != null) {
                    IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                            jsonCandidate.get("sdpMid").getAsString(),
                            jsonCandidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(candidate);
                }
                break;
            }
            default:
                sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        registry.removeBySession(session);
    }

    private void record(final WebSocketSession session, JsonObject jsonMessage) {
        try {
            // 1. Media logic (webRtcEndpoint in loopback)
            MediaPipeline pipeline = kurento.createMediaPipeline();
            WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
            webRtcEndpoint.connect(webRtcEndpoint);

            MediaProfileSpecType profile = MediaProfileSpecType.MP4;

            String fileName = jsonMessage.get("title").getAsString() + ".mp4";
            System.out.println(RECORDER_FILE_PATH + fileName);

            RecorderEndpoint recorder = new RecorderEndpoint.Builder(pipeline, RECORDER_FILE_PATH + fileName)
                    .withMediaProfile(profile).build();

            // Error listeners.
            pipeline.addErrorListener(new EventListener<ErrorEvent>() {
                @Override
                public void onEvent(ErrorEvent ev) {
                    log.error("[MediaPipeline::ErrorEvent] Error code {}: '{}', source: {}, timestamp: {}, tags: {}, description: {}",
                            ev.getErrorCode(), ev.getType(), ev.getSource().getName(),
                            ev.getTimestampMillis(), ev.getTags(), ev.getDescription());
                    sendError(session, "[MediaPipeline] " + ev.getDescription());
                }
            });
            webRtcEndpoint.addErrorListener(new EventListener<ErrorEvent>() {
                @Override
                public void onEvent(ErrorEvent ev) {
                    log.error("[WebRtcEndpoint::ErrorEvent] Error code {}: '{}', source: {}, timestamp: {}, tags: {}, description: {}",
                            ev.getErrorCode(), ev.getType(), ev.getSource().getName(),
                            ev.getTimestampMillis(), ev.getTags(), ev.getDescription());
                    sendError(session, "[WebRtcEndpoint] " + ev.getDescription());
                }
            });
            recorder.addErrorListener(new EventListener<ErrorEvent>() {
                @Override
                public void onEvent(ErrorEvent ev) {
                    log.error("[RecorderEndpoint::ErrorEvent] Error code {}: '{}', source: {}, timestamp: {}, tags: {}, description: {}",
                            ev.getErrorCode(), ev.getType(), ev.getSource().getName(),
                            ev.getTimestampMillis(), ev.getTags(), ev.getDescription());
                    sendError(session, "[RecorderEndpoint] " + ev.getDescription());
                }
            });

            recorder.addRecordingListener(new EventListener<RecordingEvent>() {

                @Override
                public void onEvent(RecordingEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "recording");
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }

            });

            recorder.addStoppedListener(new EventListener<StoppedEvent>() {

                @Override
                public void onEvent(StoppedEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "stopped");
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }

            });

            recorder.addPausedListener(new EventListener<PausedEvent>() {

                @Override
                public void onEvent(PausedEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "paused");
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }

            });

            webRtcEndpoint.connect(recorder, MediaType.AUDIO);
            webRtcEndpoint.connect(recorder, MediaType.VIDEO);

            // 2. Store user session
            UserSession user = new UserSession(session);
            user.setMediaPipeline(pipeline);
            user.setWebRtcEndpoint(webRtcEndpoint);
            user.setRecorderEndpoint(recorder);
            registry.register(user);

            // 3. SDP negotiation
            String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
            String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

            // 4. Gather ICE candidates
            webRtcEndpoint.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

                @Override
                public void onEvent(IceCandidateFoundEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "iceCandidate");
                    response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
            });

            JsonObject response = new JsonObject();
            response.addProperty("id", "startResponse");
            response.addProperty("sdpAnswer", sdpAnswer);

            synchronized (user) {
                session.sendMessage(new TextMessage(response.toString()));
            }

            webRtcEndpoint.gatherCandidates();

            recorder.record();
        } catch (Throwable t) {
            log.error("Start error", t);
            sendError(session, t.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "error");
        response.addProperty("message", message);

        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(response.toString()));
            }
        } catch (IOException e) {
            log.error("Exception sending message", e);
        }
    }

}
