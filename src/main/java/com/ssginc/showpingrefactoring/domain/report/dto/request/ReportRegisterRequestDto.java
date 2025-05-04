package com.ssginc.showpingrefactoring.domain.report.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRegisterRequestDto {
    private String reportReason;    // 신고 사유
    private String reportContent;   // 신고 대상 채팅 내용
}
