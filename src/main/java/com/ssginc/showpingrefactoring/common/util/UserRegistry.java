package com.ssginc.showpingrefactoring.common.util;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {
    private ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(UserSession user) {
        usersBySessionId.put(user.getId(), user);
    }

    public UserSession getById(String id) {
        return usersBySessionId.get(id);
    }

    public UserSession getBySession(WebSocketSession session) {
        return usersBySessionId.get(session.getId());
    }

    public boolean exists(String id) {
        return usersBySessionId.keySet().contains(id);
    }

    public UserSession removeBySession(WebSocketSession session) {
        final UserSession user = getBySession(session);
        if (user != null) {
            usersBySessionId.remove(session.getId());
        }
        return user;
    }

}
