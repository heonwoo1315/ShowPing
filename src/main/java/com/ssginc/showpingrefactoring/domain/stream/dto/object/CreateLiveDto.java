package com.ssginc.showpingrefactoring.domain.stream.dto.object;

import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.product.entity.Product;
import com.ssginc.showpingrefactoring.domain.stream.entity.Stream;
import com.ssginc.showpingrefactoring.domain.stream.entity.StreamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CreateLiveDto {

    private Member member;

    private Product product;

    private String streamTitle;

    private String streamDescription;

    private StreamStatus streamStatus;

    private LocalDateTime streamEnrollTime;

    public Stream toEntity() {
        return Stream.builder()
                .member(member)
                .product(product)
                .streamTitle(streamTitle)
                .streamDescription(streamDescription)
                .streamStatus(streamStatus)
                .streamEnrollTime(streamEnrollTime)
                .build();
    }
}
