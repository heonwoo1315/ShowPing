package com.ssginc.showpingrefactoring.domain.chat.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChatRoomResponseDto {
    private Long chatRoomNo;
    private Long streamNo;
}
