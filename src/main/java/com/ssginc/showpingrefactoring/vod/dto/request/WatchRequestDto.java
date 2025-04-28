package com.ssginc.showpingrefactoring.vod.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 시청 내역을 저장하기 위한 정보를 보내주기 위해 정의한 DTO 클래스
 * <p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchRequestDto {

    private Long streamNo;
    private LocalDateTime watchTime;

}
