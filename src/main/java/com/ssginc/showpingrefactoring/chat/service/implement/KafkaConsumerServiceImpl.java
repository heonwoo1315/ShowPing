package com.ssginc.showpingrefactoring.chat.service.implement;

import com.ssginc.showpingrefactoring.chat.dto.object.ChatDto;
import com.ssginc.showpingrefactoring.chat.repository.ChatRepository;
import com.ssginc.showpingrefactoring.chat.service.KafkaConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author juil1-kim
 * Kafka에서 수신한 채팅 메세지 소비, 이후 Websocket을 통해 client에게 실시간 전송하며
 * 몽고DB에 저장하는 클래스
 * <p>
 */
@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용

    private long kafka_time = 0;
    private long kafka_count = 0;

    /**
     * Kafka에서 ChatDto 메시지를 소비하고 처리하는 메소드
     * <p>
     * 수신된 메시지를 특정 채팅방 구독자들에게 실시간 전송하고, MongoDB에 저장.
     *
     * @param chatDto Kafka에서 수신한 채팅 메시지 객체
     */
    @Override
    @KafkaListener(topics = "chat-messages", groupId = "chat-consumer-group")
    public void consumeMessage(ChatDto chatDto) {
        long startTime = System.currentTimeMillis();
        kafka_count++;

        System.out.println("Received chat message: " + chatDto);


        try {
            // 특정 채팅방(/sub/chat/room/{chatRoomNo})으로 메시지를 실시간 전송
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatDto.getChatRoomNo(), chatDto);
            chatRepository.save(chatDto);
            System.out.println(kafka_count + "받은 chat 몽고디비에 저장됨.");

            long endTime = System.currentTimeMillis();
            System.out.println(">>>>>>> 메세지 처리시간 : " + (endTime - startTime) + "ms");
            kafka_time = endTime - startTime;

            System.out.println(kafka_count + ">> kafka_time ==========> " + kafka_time);
            System.out.println("Chat saved to MongoDB " + chatDto.getChatMessage());
        } catch (Exception e) {
            System.err.println("Error processing Kafka message: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
