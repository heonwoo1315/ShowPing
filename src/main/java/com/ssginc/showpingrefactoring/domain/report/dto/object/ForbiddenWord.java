package com.ssginc.showpingrefactoring.domain.report.dto.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "forbiddenWord") // MongoDB Collection 이름
public class ForbiddenWord {
    @Id
    private String id;        // MongoDB 자동 생성 ID

    @Field("slang")
    private String slang;      // 금칙어 단어

    public CharSequence getWord() {
        return slang;
    }
}