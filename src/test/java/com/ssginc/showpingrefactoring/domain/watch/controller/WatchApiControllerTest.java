package com.ssginc.showpingrefactoring.domain.watch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssginc.showpingrefactoring.common.TestSecurityConfig;
import com.ssginc.showpingrefactoring.common.jwt.JwtTokenProvider;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import com.ssginc.showpingrefactoring.domain.stream.entity.Stream;
import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.entity.Watch;
import com.ssginc.showpingrefactoring.domain.watch.service.WatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = WatchApiController.class)
public class WatchApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // 비로그인 시청내역 403 에러코드 테스트
    @Test
    void testGetWatchHistory_AccessForbidden() throws Exception {
        mockMvc.perform(get("/api/watch/history/list"))
                .andExpect(status().isForbidden()); // 403
    }

    // 로그인한 사용자의 시청내역 추가 테스트
    @Test
    @WithMockUser(username = "testUser")  // 로그인된 사용자로 시뮬레이션
    public void testInsertWatchHistory_WithAuthenticatedUser_ReturnsWatch() throws Exception {
        // given
        WatchRequestDto requestDto = new WatchRequestDto();
        requestDto.setStreamNo(100L);
        requestDto.setWatchTime("2025-07-30T16:00:00.000Z");

        Member mockMember = Member.builder()
                .memberNo(1L)
                .build();

        Watch mockWatch = Watch.builder()
                .stream(Stream.builder().streamNo(100L).build())
                .member(mockMember)
                .watchTime(LocalDateTime.parse("2025-07-30T16:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .build();

        // when
        when(memberService.findMemberById("testUser")).thenReturn(mockMember);
        when(watchService.insertWatchHistory(any(WatchRequestDto.class), any(Long.class))).thenReturn(mockWatch);

        // then
        mockMvc.perform(post("/api/watch/insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stream.streamNo").value(100))
                .andExpect(jsonPath("$.member.memberNo").value(1))
                .andExpect(jsonPath("$.watchTime").value("2025-07-30T16:00:00"));
    }

    // 비로그인의 시청내역 추가 테스트
    @Test
    public void testInsertWatchHistory_WithoutAuthenticatedUser_ReturnsWatchWithNullMember() throws Exception {
        // given
        WatchRequestDto requestDto = new WatchRequestDto();
        requestDto.setStreamNo(200L);
        requestDto.setWatchTime("2025-07-30T16:00:00.000Z");

        Watch mockWatch = Watch.builder()
                .stream(Stream.builder().streamNo(200L).build())
                .member(null)
                .watchTime(LocalDateTime.parse("2025-07-30T16:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .build();

        when(watchService.insertWatchHistory(any(WatchRequestDto.class), any())).thenReturn(mockWatch);

        // then
        mockMvc.perform(post("/api/watch/insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stream.streamNo").value(200))
                .andExpect(jsonPath("$.member").value(nullValue()))
                .andExpect(jsonPath("$.watchTime").value("2025-07-30T16:00:00"));
    }

}
