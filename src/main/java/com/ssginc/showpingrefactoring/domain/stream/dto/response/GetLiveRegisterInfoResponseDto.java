package com.ssginc.showpingrefactoring.domain.stream.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 방송 등록 화면에서 이미 등록한 방송 정보를 보내주기 위해 정의한 DTO 클래스
 * 가격 정보를 천 단위로 구분하여 formatting 해준 클래스
 */
@Getter
@Builder
@AllArgsConstructor
public class GetLiveRegisterInfoResponseDto {

    private Long streamNo;

    private String streamTitle;

    private String streamDescription;

    private Long productNo;

    private String productName;

    private String productPrice;

    private Integer productSale;

    private String productImg;

}
