package com.ssginc.showpingrefactoring.domain.stream.service;

import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.repository.VodRepository;
import com.ssginc.showpingrefactoring.domain.stream.service.implement.VodServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VodServiceTest {
    @Mock
    private VodRepository vodRepository;

    @InjectMocks
    private VodServiceImpl vodService;

    @Test
    void findVods_mostViewedAndCategoryExists_returnsPage() {
        // given
        Long categoryNo = 1L;
        String sort = "mostViewed";
        Pageable pageable = PageRequest.of(0, 4);
        Page<StreamResponseDto> mockPage = new PageImpl<>(List.of(new StreamResponseDto()), pageable, 1);

        when(vodRepository.findByCategoryIdOrderByViewsDesc(categoryNo, pageable)).thenReturn(mockPage);

        // when
        Page<StreamResponseDto> result = vodService.findVods(categoryNo, sort, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(vodRepository).findByCategoryIdOrderByViewsDesc(categoryNo, pageable);
    }

    @Test
    void findVods_emptyResult_throwsCustomException() {
        // given
        Long categoryNo = 0L;
        String sort = "default";
        Pageable pageable = PageRequest.of(0, 4);
        Page<StreamResponseDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(vodRepository.findAllVod(pageable)).thenReturn(emptyPage);

        // when & then
        assertThrows(CustomException.class, () -> vodService.findVods(categoryNo, sort, pageable));
    }

    // 단일 VOD 조회 성공
    @Test
    void getVodByNo_success() {
        // given
        Long streamNo = 1L;
        StreamResponseDto expectedDto = new StreamResponseDto();
        when(vodRepository.findVodByNo(streamNo)).thenReturn(expectedDto);

        // when
        StreamResponseDto result = vodService.getVodByNo(streamNo);

        // then
        assertThat(result).isEqualTo(expectedDto);
        verify(vodRepository).findVodByNo(streamNo);
    }

    // 단일 VOD 조회 실패
    @Test
    void getVodByNo_notFound_throwsException() {
        // given
        Long streamNo = 999L;
        when(vodRepository.findVodByNo(streamNo)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> vodService.getVodByNo(streamNo))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.STREAM_NOT_FOUND.getMessage());

        verify(vodRepository).findVodByNo(streamNo);
    }

}
