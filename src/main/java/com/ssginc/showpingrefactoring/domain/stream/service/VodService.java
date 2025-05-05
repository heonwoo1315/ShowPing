package com.ssginc.showpingrefactoring.domain.stream.service;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VodService {

    String uploadVideo(String title);

    List<StreamResponseDto> getAllVod();

    Page<StreamResponseDto> getAllVodByPage(Pageable pageable);

    Page<StreamResponseDto> getAllVodByWatch(Pageable pageable);

    List<StreamResponseDto> getAllVodByCategory(Long categoryNo);

    Page<StreamResponseDto> getAllVodByCategoryAndPage(Long categoryNo, Pageable pageable);

    Page<StreamResponseDto> getAllVodByCatgoryAndWatch(Long categoryNo, Pageable pageable);

    StreamResponseDto getVodByNo(Long streamNo);
}
