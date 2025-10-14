package com.ssginc.showpingrefactoring.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {

    private List<T> content;
    private PageInfo pageInfo;

    public static <T> PageResponseDto<T> of(Page<T> page) {
        return new PageResponseDto<>(page.getContent(), PageInfo.of(page));
    }
}
