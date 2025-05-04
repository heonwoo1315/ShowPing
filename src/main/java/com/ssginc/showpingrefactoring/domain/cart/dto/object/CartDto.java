package com.ssginc.showpingrefactoring.domain.cart.dto.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long productNo;
    private String productName;
    private Long productPrice;
    private Long cartProductQuantity;
    private String productImg;
    private Integer productSale;
    private Long discountedPrice;
}
