package com.ssginc.showpingrefactoring.chat.service;

import com.ssginc.showpingrefactoring.chat.dto.response.ChatRoomResponseDto;
import com.ssginc.showpingrefactoring.domain.chat.entity.ChatRoom;

/**
 * 채팅방 생성 관련 서비스 인터페이스
 */
public interface ChatRoomService {
    /**
     * 스트림 번호와 최대 참가자 수를 받아 새 채팅방을 생성합니다.
     * @param streamNo 채팅방에 연결될 영상 번호
//     * @param maxParticipants 최대 참가자 수
     * @return 생성된 ChatRoom 객체
     */
    ChatRoom createChatRoom(Long streamNo);

    ChatRoomResponseDto findChatRoomByStreamNo(Long streamNo);

}
