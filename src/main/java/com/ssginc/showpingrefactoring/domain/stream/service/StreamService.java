package com.ssginc.showpingrefactoring.domain.stream.service;

import com.ssginc.showpingrefactoring.domain.stream.dto.request.RegisterStreamRequestDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.GetStreamProductInfoResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.GetStreamRegisterInfoResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StartStreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface StreamService {

    GetStreamProductInfoResponseDto getStreamProductInfo(Long streamNo);

    StreamResponseDto getLive();

    Page<StreamResponseDto> getAllBroadCastByPage(Pageable pageable);

    Page<StreamResponseDto> getAllStandbyByPage(Pageable pageable);

    Long createStream(String memberId, RegisterStreamRequestDto request);

    StartStreamResponseDto startStream(Long streamNo);

    Boolean stopStream(Long streamNo);

    GetStreamRegisterInfoResponseDto getStreamRegisterInfo(String memberId);

}
