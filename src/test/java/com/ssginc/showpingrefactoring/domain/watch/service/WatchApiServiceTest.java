package com.ssginc.showpingrefactoring.domain.watch.service;

import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.repository.WatchRepository;
import com.ssginc.showpingrefactoring.domain.watch.service.implement.WatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class WatchApiServiceTest {
    @Mock
    private WatchRepository watchRepository;

    @InjectMocks
    private WatchServiceImpl watchService;  // 테스트 대상 Service 클래스명

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // 로그인한 시청내역 100개 조회 테스트
    @Test
    public void testGetWatchHistoryByMemberNo_For100Items() {
        Long memberNo = 1L;

        // 100개의 WatchResponseDto 준비
        List<WatchResponseDto> watchList = LongStream.rangeClosed(1, 100)
                .mapToObj(i -> {
                    WatchResponseDto dto = new WatchResponseDto();
                    return dto;
                })
                .collect(Collectors.toList());

        // Mock 동작 정의
        when(watchRepository.getWatchListByMemberNo(memberNo)).thenReturn(watchList);

        // Service 메소드 호출
        List<WatchResponseDto> result = watchService.getWatchHistoryByMemberNo(memberNo);

        // 결과 검증
        assertNotNull(result);
        assertEquals(100, result.size());
    }

    // 로그인한 시청내역 예외 테스트 (시청내역이 비어있음)
    @Test
    public void testGetWatchHistoryByMemberNo_EmptyList_ThrowsException() {
        Long memberNo = 1L;
        when(watchRepository.getWatchListByMemberNo(memberNo)).thenReturn(List.of());

        CustomException ex = assertThrows(CustomException.class, () -> {
            watchService.getWatchHistoryByMemberNo(memberNo);
        });

        assertEquals(ErrorCode.WATCH_LIST_EMPTY.getCode(), ex.getCode());
    }
}
