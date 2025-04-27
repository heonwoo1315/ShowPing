package com.ssginc.showpingrefactoring.vod.service;

import com.ssginc.showpingrefactoring.vod.dto.response.VodResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VodService {

    String uploadVideo(String title);

    List<VodResponseDto> getAllVod();

    Page<VodResponseDto> getAllVodByPage(Pageable pageable);

    Page<VodResponseDto> getAllVodByWatch(Pageable pageable);

    List<VodResponseDto> getAllVodByCategory(Long categoryNo);

    Page<VodResponseDto> getAllVodByCategoryAndPage(Long categoryNo, Pageable pageable);

    Page<VodResponseDto> getAllVodByCatgoryAndWatch(Long categoryNo, Pageable pageable);

}
