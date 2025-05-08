package com.ssginc.showpingrefactoring.domain.chat.service.implement;

import com.ssginc.showpingrefactoring.domain.chat.dto.object.ChatDto;
import com.ssginc.showpingrefactoring.domain.chat.repository.ChatRepository;
import com.ssginc.showpingrefactoring.domain.chat.service.ChatService;
import com.ssginc.showpingrefactoring.domain.report.service.ForbiddenWordFilterService;
import com.ssginc.showpingrefactoring.domain.chat.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ForbiddenWordFilterService forbiddenWordFilterService;
    private final KafkaProducerService kafkaProducerService;

    /**
     * 채팅 메시지를 저장하기 전에 금칙어 필터링을 적용.
     *
     * @param chatMemberId 전송자 아이디
     * @param chatRoomNo   채팅방 번호
     * @param chatMessage  전송할 메시지 내용
     * @param chatCreatedAt 메세지 생성 시간
     * @return 저장된 ChatDto 객체
     * @throws IllegalArgumentException 금칙어가 포함된 경우
     */
    @Override
    public ChatDto saveChatMessage(String chatMemberId, Long chatRoomNo, Long chatStreamNo ,String chatMessage, String chatRole, String chatCreatedAt) {
        // 금칙어 체크
        if (isForbiddenWordIncluded(chatMessage)) {
            List<String> foundWords = forbiddenWordFilterService.getForbiddenWords(chatMessage);
            throw new IllegalArgumentException("금칙어가 포함된 메시지는 전송할 수 없습니다. (" + foundWords + ")");
        }

        // 메시지 생성
        ChatDto message = new ChatDto();
        message.setChatMemberId(chatMemberId);
        message.setChatRoomNo(chatRoomNo);
        message.setChatStreamNo(chatStreamNo);
        message.setChatRole(chatRole);
        message.setChatMessage(chatMessage);

        // 서버에서 현재 KST 기준 시간 생성
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDateTime nowKST = LocalDateTime.now(seoulZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        chatCreatedAt = formatter.format(nowKST);
        message.setChatCreatedAt(chatCreatedAt);

        // Kafka 전송
        kafkaProducerService.sendMessage(message);
        return message;
    }

    // 금칙어 필터링 로직 (Aho-Corasick 알고리즘 기반 구현)
    private boolean isForbiddenWordIncluded(String message) {
        return forbiddenWordFilterService.containsForbiddenWord(message);
    }
}
