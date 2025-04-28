package com.ssginc.showpingrefactoring.domain.order.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderDetailId implements Serializable {

    private Long productNo;

    private Long ordersNo;

}