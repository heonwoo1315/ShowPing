package com.ssginc.showpingrefactoring.domain.member.entity;

import lombok.Getter;

@Getter
public enum SocialType {

    NAVER("네이버"),
    KAKAO("카카오");

    private final String socialType;

    SocialType(String socialType) { this.socialType = socialType; }

}
