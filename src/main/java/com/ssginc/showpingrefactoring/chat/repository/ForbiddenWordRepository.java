package com.ssginc.showpingrefactoring.chat.repository;

import com.ssginc.showpingrefactoring.chat.dto.object.ForbiddenWord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForbiddenWordRepository extends MongoRepository<ForbiddenWord, String> {
}