package com.ssginc.showpingrefactoring.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDto {
    private Long reportNo;
    private String reportCreatedAt;  // 신고 생성일
    private String reportReason;
    private String reportContent;
    private String memberId;
    private String reportStatus;
}
