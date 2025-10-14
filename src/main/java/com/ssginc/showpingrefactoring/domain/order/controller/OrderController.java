package com.ssginc.showpingrefactoring.domain.order.controller;

import com.ssginc.showpingrefactoring.domain.order.dto.request.OrderRequestDto;
import com.ssginc.showpingrefactoring.domain.order.dto.object.OrderDetailDto;
import com.ssginc.showpingrefactoring.domain.order.dto.object.OrdersDto;
import com.ssginc.showpingrefactoring.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주문 API", description = "주문 관련 API입니다.")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "회원의 모든 주문 조회", description = "회원 번호를 통해 해당 회원의 모든 주문 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
    @GetMapping("/member/{memberNo}")
    public ResponseEntity<List<OrdersDto>> getAllOrdersByMember(@PathVariable Long memberNo) {
        List<OrdersDto> orders = orderService.findAllOrdersByMember(memberNo);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "주문 상세 조회", description = "주문 번호를 통해 해당 주문의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공")
    @GetMapping("/{orderNo}/details")
    public ResponseEntity<List<OrderDetailDto>> getOrderDetails(@PathVariable Long orderNo) {
        List<OrderDetailDto> orderDetails = orderService.findOrderDetailsByOrder(orderNo);
        return ResponseEntity.ok(orderDetails);
    }

    @Operation(summary = "주문 생성", description = "주문 정보를 받아 새 주문을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "주문 생성 성공")
    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        orderService.createOrder(orderRequestDto);
        return ResponseEntity.ok("주문이 성공적으로 저장되었습니다.");
    }
}
