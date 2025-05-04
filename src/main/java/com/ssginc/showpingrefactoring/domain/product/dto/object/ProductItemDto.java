package com.ssginc.showpingrefactoring.domain.product.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemDto {

    private Long productNo;

    private String productName;

    private Long productPrice;

    private String productImg;

}
