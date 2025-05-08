package com.ssginc.showpingrefactoring.domain.order.service.implement;

import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.order.dto.request.OrderRequestDto;
import com.ssginc.showpingrefactoring.domain.order.dto.object.OrderDetailDto;
import com.ssginc.showpingrefactoring.domain.order.dto.object.OrdersDto;
import com.ssginc.showpingrefactoring.domain.member.repository.MemberRepository;
import com.ssginc.showpingrefactoring.domain.order.entity.OrderDetail;
import com.ssginc.showpingrefactoring.domain.order.entity.OrderDetailId;
import com.ssginc.showpingrefactoring.domain.order.entity.OrderStatus;
import com.ssginc.showpingrefactoring.domain.order.entity.Orders;
import com.ssginc.showpingrefactoring.domain.order.repository.OrderDetailRepository;
import com.ssginc.showpingrefactoring.domain.order.repository.OrdersRepository;
import com.ssginc.showpingrefactoring.domain.product.entity.Product;
import com.ssginc.showpingrefactoring.domain.product.repository.ProductRepository;
import com.ssginc.showpingrefactoring.domain.order.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    private final OrdersRepository ordersRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Override
    public List<OrdersDto> findAllOrdersByMember(Long memberNo) {
        List<Orders> ordersList = ordersRepository.findByMember_MemberNoOrderByOrdersDateDesc(memberNo);
        return ordersList.stream().map(OrdersDto::new).collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailDto> findOrderDetailsByOrder(Long orderNo) {
        Optional<Orders> order = ordersRepository.findById(orderNo);
        return order.map(o -> orderDetailRepository.findByOrder(o)
                        .stream()
                        .map(OrderDetailDto::new)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    @Transactional
    @Override
    public void createOrder(OrderRequestDto orderRequestDto) {

        // 회원 조회 (예외 발생 가능)
        Member member = memberRepository.findById(orderRequestDto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다: " + orderRequestDto.getMemberNo()));

        // 주문 저장
        Orders order = new Orders();
        order.setMember(member);
        order.setOrdersTotalPrice(orderRequestDto.getTotalPrice());
        order.setOrdersDate(LocalDateTime.now());
        order.setOrdersStatus(OrderStatus.READY);

        // 주문을 먼저 저장하고, ID 값을 가져옴
        Orders savedOrder = ordersRepository.save(order);

        // 주문 상세 저장
        List<OrderDetail> orderDetails = orderRequestDto.getOrderItems().stream().map(item -> {
            if (item.getProductNo() == null) {
                throw new IllegalArgumentException("상품 번호가 없습니다: " + item);
            }

            Product product = productRepository.findById(item.getProductNo())
                    .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다: " + item.getProductNo()));

            // OrderDetailId 객체 생성
            OrderDetailId orderDetailId = new OrderDetailId(product.getProductNo(), savedOrder.getOrdersNo());

            OrderDetail detail = new OrderDetail();
            detail.setOrderDetailId(orderDetailId); // 복합 키 설정
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setOrderDetailQuantity(item.getQuantity());
            detail.setOrderDetailTotalPrice(item.getTotalPrice());

            return detail;
        }).collect(Collectors.toList());

        orderDetailRepository.saveAll(orderDetails);
        System.out.println("저장된 주문 상세 개수: " + orderDetails.size());
    }
}
