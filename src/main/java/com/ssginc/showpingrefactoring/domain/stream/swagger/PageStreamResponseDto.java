package com.ssginc.showpingrefactoring.domain.stream.swagger;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class PageStreamResponseDto extends PageImpl<StreamResponseDto> {
    public PageStreamResponseDto(List<StreamResponseDto> content) {
        super(content);
    }
}
