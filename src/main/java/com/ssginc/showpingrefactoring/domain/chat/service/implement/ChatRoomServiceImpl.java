package com.ssginc.showpingrefactoring.domain.chat.service.implement;

import com.ssginc.showpingrefactoring.domain.chat.dto.response.ChatRoomResponseDto;
import com.ssginc.showpingrefactoring.domain.chat.entity.ChatRoom;
import com.ssginc.showpingrefactoring.domain.stream.entity.Stream;
import com.ssginc.showpingrefactoring.domain.chat.repository.ChatRoomRepository;

import com.ssginc.showpingrefactoring.domain.chat.service.ChatRoomService;
import com.ssginc.showpingrefactoring.domain.stream.repository.LiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final LiveRepository liveRepository; // 영상 정보 조회용

    /**
     * 스트림 번호와 최대 참가자 수를 기반으로 새 채팅방을 생성합니다.
     * @param streamNo 채팅방에 연결될 영상 번호
    //     * @param maxParticipants 최대 참가자 수
     * @return 생성된 ChatRoom 객체
     */
    @Override
    public ChatRoom createChatRoom(Long streamNo ) {
        // 영상 엔티티 조회
        Stream stream = liveRepository.findById(streamNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 streamNo: " + streamNo));

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setStream(stream);
        chatRoom.setChatRoomCreatedAt(LocalDateTime.now());
//        chatRoom.setChatRoomMaxParticipants(maxParticipants);

        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoomResponseDto findChatRoomByStreamNo(Long streamNo) {
        return chatRoomRepository.findChatRoomByStreamNo(streamNo);
    }

}
