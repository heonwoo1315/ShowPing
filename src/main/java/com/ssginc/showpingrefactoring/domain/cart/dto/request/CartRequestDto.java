package com.ssginc.showpingrefactoring.domain.cart.dto.request;

import lombok.Data;

@Data
public class CartRequestDto {
    private Long productNo;
    private Long quantity;
}
