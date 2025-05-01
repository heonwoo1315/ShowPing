package com.ssginc.showpingrefactoring.chat.service;

import com.ssginc.showpingrefactoring.chat.dto.object.ChatDto;

/**
 * @author juil1-kim
 * Kafka에서 수신한 채팅 메세지 소비, 이후 Websocket을 통해 client에게 실시간 전송하며
 * 몽고DB에 저장하는 클래스
 * <p>
 */
public interface KafkaConsumerService {

    /**
     * Kafka에서 ChatDto 메시지를 소비하고 처리하는 메소드
     * <p>
     * 수신된 메시지를 특정 채팅방 구독자들에게 실시간 전송하고, MongoDB에 저장.
     *
     * @param chatDto Kafka에서 수신한 채팅 메시지 객체
     */

    void consumeMessage(ChatDto chatDto);

}