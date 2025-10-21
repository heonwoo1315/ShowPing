package com.ssginc.showpingrefactoring.domain.member.service;

public interface InviteService {
    String issue(Long memberNo);
    void validate(Long memberNo, String token);
    void complete(String inviteId);
}
