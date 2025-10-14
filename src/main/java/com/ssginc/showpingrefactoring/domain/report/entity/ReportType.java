package com.ssginc.showpingrefactoring.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportType {

    CHAT("채팅"),
    REVIEW("리뷰");

    private final String reportType;

    ReportType(String reportType) {
        this.reportType = reportType;
    }

}
