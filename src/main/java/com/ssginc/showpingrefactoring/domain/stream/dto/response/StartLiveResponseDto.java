package com.ssginc.showpingrefactoring.domain.stream.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 방송 시작 후 최신화된 방송 정보를 보내주기 위해 정의한 DTO 클래스
 */
@Getter
@Builder
@AllArgsConstructor
public class StartLiveResponseDto {

    private String streamTitle;

    private String streamDescription;

    private String productImg;

    private Long productNo;

    private String productName;

    private String productPrice;

    private Integer productSale;

}
