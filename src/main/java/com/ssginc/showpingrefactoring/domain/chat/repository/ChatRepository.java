package com.ssginc.showpingrefactoring.domain.chat.repository;

import com.ssginc.showpingrefactoring.domain.chat.dto.object.ChatDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<ChatDto, String> {
    List<ChatDto> findByChatStreamNo(Long chatStreamNo);
}