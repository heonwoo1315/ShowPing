package com.ssginc.showpingrefactoring.domain.order.dto.request;

import com.ssginc.showpingrefactoring.domain.order.dto.object.OrderItemDto;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {
    private Long memberNo;
    private Long totalPrice;
    private List<OrderItemDto> orderItems;
}
