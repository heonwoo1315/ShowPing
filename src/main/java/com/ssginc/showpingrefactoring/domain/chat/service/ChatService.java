package com.ssginc.showpingrefactoring.domain.chat.service;

import com.ssginc.showpingrefactoring.domain.chat.dto.object.ChatDto;

public interface ChatService {
    ChatDto saveChatMessage(String chatMemberId, Long chatRoomNo, Long chatStreamNo ,String chatMessage, String chatRole, String chatCreatedAt);
}
