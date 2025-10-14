package com.ssginc.showpingrefactoring.domain.chat.controller;

import com.ssginc.showpingrefactoring.domain.chat.dto.response.ChatRoomResponseDto;
import com.ssginc.showpingrefactoring.domain.chat.entity.ChatRoom;
import com.ssginc.showpingrefactoring.domain.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Tag(name = "ChatRoom", description = "채팅방 관리 API")
@Controller
@RequiredArgsConstructor
@RequestMapping("api/chatRoom")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;


    /**
     * 채팅방 생성 API
     * @param data 요청 데이터
     * @return 생성된 ChatRoom 객체 (JSON)
     */
    @Operation(
            summary = "채팅방 생성",
            description = "streamNo를 기반으로 새로운 채팅방 생성."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공", content = @Content(schema = @Schema(implementation = ChatRoomResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createChatRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "채팅방 생성 요청 데이터. streamNo(Long) 필요",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
            @RequestBody Map<String, Long> data) {

        Long streamNo = data.get("streamNo");
        try {
            ChatRoom newRoom = chatRoomService.createChatRoom(streamNo);
            if (newRoom != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(ChatRoomResponseDto.builder()
                        .chatRoomNo(newRoom.getChatRoomNo())
                        .streamNo(streamNo).build());
            }
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 생성 중 오류 발생: " + e.getMessage());
        }
    }
}