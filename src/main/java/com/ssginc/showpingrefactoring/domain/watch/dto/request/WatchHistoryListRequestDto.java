package com.ssginc.showpingrefactoring.domain.watch.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistoryListRequestDto {

    @NotNull(message = "페이지 번호는 필수입니다.")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int pageNo;

    @NotNull(message = "페이지 크기는 필수입니다.")
    @Min(value = 0, message = "페이지 크기는 0 이상이어야 합니다.")
    private int pageSize;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime toDate;

    @Pattern(regexp = "^(recent)$", message = "sort는 recent 이어야 합니다.")
    private String sort = "recent";

}
