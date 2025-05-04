package com.ssginc.showpingrefactoring.domain.report.repository;

import com.ssginc.showpingrefactoring.domain.report.dto.object.ForbiddenWord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForbiddenWordRepository extends MongoRepository<ForbiddenWord, String> {
}