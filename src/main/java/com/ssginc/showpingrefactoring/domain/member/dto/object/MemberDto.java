package com.ssginc.showpingrefactoring.domain.member.dto.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberDto {
    private Long memberNo;
    private String memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private String memberAddress;
}
