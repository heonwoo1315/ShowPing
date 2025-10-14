package com.ssginc.showpingrefactoring.domain.order.dto.object;

import lombok.Data;

@Data
public class OrderItemDto {
    private Long productNo;
    private Long quantity;
    private Long totalPrice;

    public OrderItemDto(long l, long l1, long l2) {
        this.productNo = l;
        this.quantity = l1;
        this.totalPrice = l2;
    }
}