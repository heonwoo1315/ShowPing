package com.ssginc.showpingrefactoring.domain.stream.controller;

import com.ssginc.showpingrefactoring.common.jwt.JwtFilter;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.service.SubtitleService;
import com.ssginc.showpingrefactoring.domain.stream.service.VodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VodApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VodApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VodService vodService;

    @MockBean
    private SubtitleService subtitleService;

    // JwtFilter를 MockBean으로 등록하여 실제 인증 우회
    @MockBean
    private JwtFilter jwtFilter;

    // 정상적으로 페이지네이션 확인 테스트
    @Test
    void listVod_success() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 4);
        StreamResponseDto dto = new StreamResponseDto(); // 실제 필드에 맞게 생성
        Page<StreamResponseDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(vodService.findVods(1L, "recent", pageable)).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/vod/list")
                        .param("pageNo", "0")
                        .param("categoryNo", "1")
                        .param("sort", "recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageInfo.content").isArray())
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1));
    }

    // 유효성 에러에 따른 예외처리 확인
    @Test
    void listVod_bindException_whenPageNoNegative() throws Exception {
        mockMvc.perform(get("/api/vod/list")
                        .param("pageNo", "-1")
                        .param("categoryNo", "1")
                        .param("sort", "recent"))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertThat(result.getResolvedException()).isInstanceOf(BindException.class)
                );
    }
}
