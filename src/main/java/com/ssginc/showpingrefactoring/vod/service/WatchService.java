package com.ssginc.showpingrefactoring.vod.service;

import com.ssginc.showpingrefactoring.vod.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.vod.dto.response.WatchResponseDto;

import java.util.List;

public interface WatchService {

    List<WatchResponseDto> getWatchHistoryByMemberNo(Long memberNo);

    Watch insertWatchHistory(WatchRequestDto watchRequestDto, Long memberNo);

}
