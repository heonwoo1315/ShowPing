package com.ssginc.showpingrefactoring.domain.chat.controller;

import com.ssginc.showpingrefactoring.domain.chat.dto.object.ChatDto;
import com.ssginc.showpingrefactoring.domain.chat.repository.ChatRepository;
import com.ssginc.showpingrefactoring.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juil1-kim
 * 채팅 관련 요청-응답 수행하는 Controller 클래스
 * <p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("chat") // REST API의 기본 경로 설정
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송
    private final ChatService chatService; // 채팅 서비스 로직
    private final ChatRepository chatRepository;

    /**
     * 채팅 메시지를 저장하는 공통 로직 처리 메소드
     *
     * @param chatDto 채팅 메시지 객체 (전송자, 채팅방 번호, 메시지 내용, 생성시간)
     * @return 저장된 채팅 메시지 객체
     */
    private ChatDto processAndSaveMessage(ChatDto chatDto) {
        return chatService.saveChatMessage(
                chatDto.getChatMemberId(),
                chatDto.getChatRoomNo(),
                chatDto.getChatStreamNo(),
                chatDto.getChatMessage(),
                chatDto.getChatRole(),
                chatDto.getChatCreatedAt()
        );
    }

    // streamNo(채팅방 번호) 기반으로 채팅 메시지 조회 엔드포인트
    @GetMapping("api/messages")
    public List<ChatDto> getChatMessages(@RequestParam Long chatStreamNo) { // streamNo == chatRoomNo로 가정
        return chatRepository.findByChatStreamNo(chatStreamNo);
    }

    @GetMapping("api/info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("memberId", userDetails.getUsername());
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");
        result.put("role", role);
        return ResponseEntity.ok(result);
    }

    /**
     * WebSocket을 통해 클라이언트에서 "/chat/message"로 전송된 메시지를 처리하는 메소드
     * <p>
     * 메시지를 처리하고 저장한 후, 해당 채팅방에 메시지를 브로드캐스트.
     * 금칙어가 포함된 경우, 해당 유저에게 에러 메시지를 전송.
     *
     * @param message   클라이언트에서 전송한 채팅 메시지 객체
     * @param principal 현재 WebSocket 연결 사용자 정보
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatDto message, Principal principal) {
        try {
            ChatDto savedMessage = processAndSaveMessage(message);
        } catch (IllegalArgumentException e) {
            ChatDto errorResponse = new ChatDto();
            errorResponse.setChatMessage("금칙어가 포함된 메시지는 전송할 수 없습니다.");
            // 해당 사용자 채널에 메시지 전송
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", errorResponse);
            } else {
                messagingTemplate.convertAndSend("/topic/errors", errorResponse);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in sendMessage: " + e);
        }
    }
}