package com.ssginc.showpingrefactoring.chat.service;

import com.ssginc.showpingrefactoring.chat.dto.object.ChatDto;

public interface ChatService {
    ChatDto saveChatMessage(String chatMemberId, Long chatRoomNo, Long chatStreamNo ,String chatMessage, String chatRole, String chatCreatedAt);
}
