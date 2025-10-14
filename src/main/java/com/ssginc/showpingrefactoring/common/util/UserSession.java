package com.ssginc.showpingrefactoring.common.util;import com.google.gson.JsonObject;
import org.kurento.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UserSession {
    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    private final WebSocketSession session;

    private String id;
    private WebRtcEndpoint webRtcEndpoint;
    private RecorderEndpoint recorderEndpoint;
    private MediaPipeline mediaPipeline;
    private Date stopTimestamp;

    public UserSession(WebSocketSession session) {
        this.session = session;
        this.id = session.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WebRtcEndpoint getWebRtcEndpoint() {
        return webRtcEndpoint;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
        this.webRtcEndpoint = webRtcEndpoint;
    }

    public void setRecorderEndpoint(RecorderEndpoint recorderEndpoint) {
        this.recorderEndpoint = recorderEndpoint;
    }

    public MediaPipeline getMediaPipeline() {
        return mediaPipeline;
    }

    public void setMediaPipeline(MediaPipeline mediaPipeline) {
        this.mediaPipeline = mediaPipeline;
    }

    public void addCandidate(IceCandidate candidate) {
        webRtcEndpoint.addIceCandidate(candidate);
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("Sending message from user with session Id '{}': {}", session.getId(), message);
        session.sendMessage(new TextMessage(message.toString()));
    }

    public Date getStopTimestamp() {
        return stopTimestamp;
    }

    public void stop() {
        if (recorderEndpoint != null) {
            final CountDownLatch stoppedCountDown = new CountDownLatch(1);
            ListenerSubscription subscriptionId = recorderEndpoint
                    .addStoppedListener(new EventListener<StoppedEvent>() {

                        @Override
                        public void onEvent(StoppedEvent event) {
                            stoppedCountDown.countDown();
                        }
                    });
            recorderEndpoint.stop();
            try {
                if (!stoppedCountDown.await(5, TimeUnit.SECONDS)) {
                    log.error("Error waiting for recorder to stop");
                }
            } catch (InterruptedException e) {
                log.error("Exception while waiting for state change", e);
            }
            recorderEndpoint.removeStoppedListener(subscriptionId);
        }
    }

    public void release() {
        this.mediaPipeline.release();
        this.webRtcEndpoint = null;
        this.mediaPipeline = null;
        if (this.stopTimestamp == null) {
            this.stopTimestamp = new Date();
        }
    }
}
