package com.ssginc.showpingrefactoring.domain.stream.dto.object;

import lombok.*;

/**
 * 이미 등록한 방송 정보를 가져오기 위해 정의한 DTO 클래스
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GetLiveRegisterInfoDto {

    private Long streamNo;

    private String streamTitle;

    private String streamDescription;

    private Long productNo;

    private String productName;

    private Long productPrice;

    private Integer productSale;

    private String productImg;

}
