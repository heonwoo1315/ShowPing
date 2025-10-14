package com.ssginc.showpingrefactoring.infrastructure.NCP.subtitle;


import lombok.*;

import java.util.List;

/**
 * @author dckat
 * segment별 자막정보를 저장한 클래스
 * <p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Segments {

    private Long start;             // 시작 시간
    private Long end;               // 끝 시간
    private String text;            // 자막 내용
    private List<Words> words;      // 문장별 단어

}
