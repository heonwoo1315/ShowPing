package com.ssginc.showpingrefactoring.domain.product.dto.object;

import com.ssginc.showpingrefactoring.domain.product.entity.Product;
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

    public static ProductItemDto fromEntity(Product product) {
        return ProductItemDto.builder()
                .productNo(product.getProductNo())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .productImg(product.getProductImg())
                .build();
    }

}
