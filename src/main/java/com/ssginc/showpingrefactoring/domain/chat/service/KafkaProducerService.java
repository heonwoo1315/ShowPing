package com.ssginc.showpingrefactoring.chat.service;

import com.ssginc.showpingrefactoring.domain.chat.dto.object.ChatDto;

/**
 * @author juil1-kim
 * Kafka를 이용하여 ChatDto 전송하는 Producer 클래스
 * <p>
 */

public interface KafkaProducerService {
    /**
     * ChatDto 메시지를 Kafka로 전송
     * @param chatDto 전송할 채팅 메시지 객체
     */
    void sendMessage(ChatDto chatDto);
}
