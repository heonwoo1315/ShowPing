package com.ssginc.showpingrefactoring.chat.dto.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {
    private String searchKeyword;
    private String searchCategory;
    private String dateType;
    private String startDate;
    private String endDate;
    private String status = "all"; // 기본값 설정
}