package com.ssginc.showpingrefactoring.controller;

import com.ssginc.showpingrefactoring.domain.member.controller.MemberController;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    void 아이디_중복시_400응답을_반환한다() throws Exception {
        // given
        String memberId = "duplicateUser";
        given(memberService.isDuplicateId(memberId)).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/member/check-duplicate")
                        .param("id", memberId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("중복된 아이디입니다"));
    }

    @Test
    void 전화번호_중복이_없을때_200응답을_반환한다() throws Exception {
        // given
        String phone = "010-9999-0000";
        given(memberService.isDuplicatePhone(phone)).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/member/check-phone-duplicate")
                        .param("phone", phone))
                .andExpect(status().isOk())
                .andExpect(content().string("사용 가능한 전화번호입니다."));
    }
}
