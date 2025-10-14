package com.ssginc.showpingrefactoring.domain.order.service;

import jakarta.transaction.Transactional;
import java.util.List;
import com.ssginc.showpingrefactoring.domain.order.dto.request.OrderRequestDto;
import com.ssginc.showpingrefactoring.domain.order.dto.object.OrderDetailDto;
import com.ssginc.showpingrefactoring.domain.order.dto.object.OrdersDto;

public interface OrderService {
    List<OrdersDto> findAllOrdersByMember(Long memberNo);

    List<OrderDetailDto> findOrderDetailsByOrder(Long orderNo);

    @Transactional
    void createOrder(OrderRequestDto orderRequestDto);
}
