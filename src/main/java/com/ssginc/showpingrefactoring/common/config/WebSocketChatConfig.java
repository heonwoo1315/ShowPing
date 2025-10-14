package com.ssginc.showpingrefactoring.common.config;

import com.ssginc.showpingrefactoring.common.handler.UserCustomHandshakeHandler;
import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * @author juil1-kim
 * Websocket을 이용하여 채팅 메세지 브로커 설정 담당 클래스
 * <p>
 * STOMP를 활용하여 Client-Server간의 Websocket 연결 및 메세지 브로커 구성을 수행
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketChatConfig implements WebSocketMessageBrokerConfigurer {


    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 메시지 브로커의 구성 설정을 수행하는 메소드
     * <p>
     * 클라이언트로부터의 발행 메시지는 "/pub" prefix를 사용,
     * 서버가 구독하는 클라이언트에는 "/sub" prefix를 사용하도록 설정.
     *
     * @param registry 메시지 브로커 설정을 위한 MessageBrokerRegistry 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "/topic", "/queue"); // 구독 prefix
        registry.setApplicationDestinationPrefixes("/pub"); // 발행 prefix
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP 엔드포인트를 등록하는 메소드
     * <p>
     * 클라이언트가 WebSocket 연결을 시작할 수 있도록 "/ws-stomp-chat" 엔드포인트를 노출하고,
     * SockJS를 사용하여 브라우저 호환성을 지원.
     *
     * @param registry STOMP 엔드포인트 등록을 위한 StompEndpointRegistry 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp-chat") // WebSocket 연결 Endpoint
                .setAllowedOriginPatterns("*")
                .addInterceptors(webSocketAuthInterceptor()) // 인증 인터셉터
                .setHandshakeHandler(userCustomHandshakeHandler())
                .withSockJS();
    }

    @Bean
    public HandshakeInterceptor webSocketAuthInterceptor() {
        return new WebSocketAuthInterceptorConfig(jwtTokenProvider);
    }

    @Bean
    public UserCustomHandshakeHandler userCustomHandshakeHandler() {
        return new UserCustomHandshakeHandler();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebsocketUserInterceptorConfig());  // 유저 채널 인터셉터 등록
    }
}
