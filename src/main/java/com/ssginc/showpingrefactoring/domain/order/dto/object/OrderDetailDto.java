package com.ssginc.showpingrefactoring.domain.order.dto.object;

import com.ssginc.showpingrefactoring.domain.order.entity.OrderDetail;
import lombok.Getter;


@Getter
public class OrderDetailDto {
    private String productName;
    private Long orderDetailQuantity;
    private Long orderDetailTotalPrice;

    public OrderDetailDto(OrderDetail orderDetail) {
        this.productName = orderDetail.getProduct().getProductName();
        this.orderDetailQuantity = orderDetail.getOrderDetailQuantity();
        this.orderDetailTotalPrice = orderDetail.getOrderDetailTotalPrice();
    }
}
