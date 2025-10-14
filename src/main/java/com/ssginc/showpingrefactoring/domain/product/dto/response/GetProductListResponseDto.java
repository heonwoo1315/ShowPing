package com.ssginc.showpingrefactoring.domain.product.dto.response;

import com.ssginc.showpingrefactoring.domain.product.dto.object.ProductItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetProductListResponseDto {

    private List<ProductItemDto> data;

    private boolean hasNext;

    private Long nextLastProductNo;

}
