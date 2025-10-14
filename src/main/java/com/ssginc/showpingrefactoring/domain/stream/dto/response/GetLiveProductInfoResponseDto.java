package com.ssginc.showpingrefactoring.domain.stream.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 방송 시청 화면에서 해당 방송의 상품 정보를 보내주기 위해 정의한 DTO 클래스
 * 가격 정보를 천 단위로 구분하여 formatting 해준 클래스
 */
@Getter
@Builder
@AllArgsConstructor
public class GetLiveProductInfoResponseDto {

    private Long productNo;

    private String productImg;

    private String productName;

    private String productPrice;

    private String productSalePrice;

}