package com.ssginc.showpingrefactoring.chat.repository;

import com.ssginc.showpingrefactoring.chat.dto.response.ChatRoomResponseDto;
import com.ssginc.showpingrefactoring.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT new com.ssginc.showpingrefactoring.chat.dto.response.ChatRoomResponseDto(
            c.chatRoomNo, c.stream.streamNo
        ) FROM ChatRoom c WHERE c.stream.streamNo = :streamNo
    """)
    ChatRoomResponseDto findChatRoomByStreamNo(Long streamNo);

}