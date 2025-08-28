package com.ssginc.showpingrefactoring.domain.watch.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 시청 내역을 저장하기 위한 정보를 보내주기 위해 정의한 DTO 클래스
 * <p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchRequestDto {

    @NotNull(message = "영상 번호는 필수입니다.")
    @Min(value = 1, message = "영상 번호는 1 이상이어야 합니다.")
    private Long streamNo;

    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$",
            message = "watchTime은 yyyy-MM-dd'T'HH:mm:ss.SSSZ 포맷이어야 합니다."
    )
    private String watchTime;

}
