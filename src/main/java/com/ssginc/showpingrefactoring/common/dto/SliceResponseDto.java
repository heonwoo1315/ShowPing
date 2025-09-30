package com.ssginc.showpingrefactoring.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SliceResponseDto<T, C> {
    private List<T> content;
    private boolean hasMore;
    private C nextCursor;

    public static <T, C> SliceResponseDto<T, C> of(List<T> content, boolean hasMore, C nextCursor) {
        return new SliceResponseDto<T, C>(content, hasMore, nextCursor);
    }

}
