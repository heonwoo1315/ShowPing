package com.ssginc.showpingrefactoring.domain.member.dto.object;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private Long memberNo;
    private String memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private String memberAddress;
}
