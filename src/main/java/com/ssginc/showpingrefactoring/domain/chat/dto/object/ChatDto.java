package com.ssginc.showpingrefactoring.domain.chat.dto.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author juil1-kim
 *  몽고DB Chat Collection으로 보낼 Dto 클래스
 * <p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat") // MongoDB Collection 이름
public class ChatDto {
    @JsonProperty("_id")
    private String id; // MongoDB의 _id 필드 (ObjectId를 String으로 매핑)

    @JsonProperty("chat_member_id")
    private String chatMemberId;

    @JsonProperty("chat_room_no")
    private Long chatRoomNo;

    @JsonProperty("chat_stream_no")
    private Long chatStreamNo;

    @JsonProperty("chat_message")
    private String chatMessage;

    @JsonProperty("chat_role")
    private String chatRole;

    @JsonProperty("chat_created_at")
    private String chatCreatedAt; // 생성 시간
}