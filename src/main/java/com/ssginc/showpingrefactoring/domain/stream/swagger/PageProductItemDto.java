package com.ssginc.showpingrefactoring.domain.stream.swagger;

import com.ssginc.showpingrefactoring.domain.product.dto.object.ProductItemDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class PageProductItemDto extends PageImpl<ProductItemDto> {
    public PageProductItemDto(List<ProductItemDto> content) {
        super(content);
    }
}
