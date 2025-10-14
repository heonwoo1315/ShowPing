package com.ssginc.showpingrefactoring.service;

import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.member.service.implement.MemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void 아이디가_중복되었을때_true를_반환한다() {
        // given
        String memberId = "testuser";
        given(memberRepository.existsByMemberId(memberId)).willReturn(true);

        // when
        boolean result = memberService.isDuplicateId(memberId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 전화번호가_중복되지_않았을때_false를_반환한다() {
        // given
        String phone = "010-1234-5678";
        given(memberRepository.existsByMemberPhone(phone)).willReturn(false);

        // when
        boolean result = memberService.isDuplicatePhone(phone);

        // then
        assertThat(result).isFalse();
    }
}