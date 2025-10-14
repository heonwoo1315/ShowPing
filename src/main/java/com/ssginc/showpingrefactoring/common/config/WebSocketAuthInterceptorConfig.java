package com.ssginc.showpingrefactoring.common.config;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class WebSocketAuthInterceptorConfig implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = null;

        // 1. Authorization 헤더에서 토큰 읽기
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty() && authHeaders.get(0).startsWith("Bearer ")) {
            token = authHeaders.get(0).substring(7);
        }

        // 2. 헤더에서 없으면 URL 쿼리 파라미터에서 읽기
        if (token == null) {
            String query = request.getURI().getQuery(); // 예: access_token=abc123
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("access_token=")) {
                        token = param.split("=")[1];
                        break;
                    }
                }
            }
        }

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userId = jwtTokenProvider.getUsername(token); // 토큰에서 사용자 ID 추출
            if (userId != null && !userId.isEmpty()) {
                attributes.put("user", new StompPrincipal(userId)); // 사용자 ID를 Principal로 설정
            } else {
                System.err.println("[ERROR] Invalid token: no userId found.");
            }
        } else {
            System.err.println("[DEBUG] No valid token provided; Principal remains null.");
        }
        // 항상 핸드쉐이크를 허용(Principal이 null이어도 연결은 유지)
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}

class StompPrincipal implements java.security.Principal {
    private final String memberId;
    public StompPrincipal(String memberId) { this.memberId = memberId; }
    @Override
    public String getName() { return memberId; } // getName이지만 실제로는 memberId
}