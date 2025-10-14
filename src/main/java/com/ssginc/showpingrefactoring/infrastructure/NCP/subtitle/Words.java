package com.ssginc.showpingrefactoring.infrastructure.NCP.subtitle;

import lombok.*;

/**
 * @author dckat
 * segment 내의 어절별 자막 정보를 담은 클래스
 * <p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Words {

    private Long start;         // 어절의 시작 시간
    private Long end;           // 어절의 시간
    private String text;        // 어절 내용

}
