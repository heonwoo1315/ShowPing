package com.ssginc.showpingrefactoring.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;

@Configuration
public class WebsocketUserInterceptorConfig implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getUser() == null) {
            // WebSocket 세션에서 Principal을 가져옴
            Principal user = (Principal) accessor.getSessionAttributes().get("user");

            if (user != null) {
                System.out.println("[DEBUG] WebsocketUserInterceptorConfig: Setting Principal: " + user.getName());
                accessor.setUser(user);
            } else {
                System.err.println("[ERROR] WebsocketUserInterceptorConfig: Principal is still null!");
            }
        }

        return message;
    }
}
