package com.ssginc.showpingrefactoring.chat.service.implement;

import com.ssginc.showpingrefactoring.chat.dto.object.ChatDto;
import com.ssginc.showpingrefactoring.chat.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author juil1-kim
 * Kafka를 이용하여 ChatDto 전송하는 Producer 클래스
 * <p>
 */
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, ChatDto> kafkaTemplate;
    private static final String TOPIC = "chat-messages"; // Kafka 토픽 이름

    /**
     * ChatDto 메시지를 Kafka로 전송
     * @param chatDto 전송할 채팅 메시지 객체
     */
    @Override
    public void sendMessage(ChatDto chatDto) {
        try {
            kafkaTemplate.send(TOPIC, chatDto); // Kafka로 메시지 전송
        } catch (Exception e) {
            System.err.println("Error sending message to Kafka: " + e.getMessage());
        }
    }
}
