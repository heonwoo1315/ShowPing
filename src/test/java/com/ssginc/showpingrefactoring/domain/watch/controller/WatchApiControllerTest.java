package com.ssginc.showpingrefactoring.domain.watch.controller;

import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.service.WatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WatchApiController.class)
public class WatchApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WatchService watchService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetails userDetails;

    private List<WatchResponseDto> mockWatchList;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 WatchResponseDto 리스트 생성
        mockWatchList = List.of(
                new WatchResponseDto(),
                new WatchResponseDto()
        );
    }

    @Test
    @WithMockUser(username = "testUser")  // 인증된 사용자로 테스트
    public void testGetWatchHistory_ReturnsHistoryList() throws Exception {
        // Member 객체 Mock
        Member mockMember = new Member();
        mockMember.setMemberNo(1L);
        when(memberService.findMemberById("testUser")).thenReturn(mockMember);

        when(watchService.getWatchHistoryByMemberNo(1L)).thenReturn(mockWatchList);

        mockMvc.perform(get("/api/watch/history/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historyList").isArray())
                .andExpect(jsonPath("$.historyList.length()").value(mockWatchList.size()));
    }

}
