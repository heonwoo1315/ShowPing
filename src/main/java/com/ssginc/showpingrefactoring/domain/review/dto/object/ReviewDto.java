package com.ssginc.showpingrefactoring.domain.review.dto.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long reviewNo;
    private String memberName;
    private Long reviewRating;
    private String reviewComment;
    private LocalDateTime reviewCreateAt;
    private String reviewUrl;
}