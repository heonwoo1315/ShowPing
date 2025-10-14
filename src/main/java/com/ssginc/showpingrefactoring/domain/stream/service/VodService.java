package com.ssginc.showpingrefactoring.domain.stream.service;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VodService {

    String uploadVideo(String title);

    Page<StreamResponseDto> findVods(@Min(value = 0) Long categoryNo, String sort, Pageable pageable);

    StreamResponseDto getVodByNo(Long streamNo);

}
