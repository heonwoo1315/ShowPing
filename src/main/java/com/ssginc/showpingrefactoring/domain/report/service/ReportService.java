package com.ssginc.showpingrefactoring.domain.report.service;

import com.ssginc.showpingrefactoring.domain.report.dto.request.ReportRegisterRequestDto;
import com.ssginc.showpingrefactoring.domain.report.entity.Report;

import java.util.List;

/**
 * @author 신고 관리 서비스 인터페이스
 * <p>
 */
public interface ReportService {

    /**
     * 전체 신고 목록 조회.
     * * @return 전체 신고 리스트
     */
    List<Report> getAllReports();

    /**
     * 검색 조건에 따른 신고 필터링.
     * * @param searchKeyword  검색어
     *
     * @param searchCategory 검색 카테고리 (reportNum, receipt, reason, writer, proceed)
     * @param dateType       날짜 유형 ("접수일")
     * @param startDate      시작일 (yyyy-MM-dd)
     * @param endDate        종료일 (yyyy-MM-dd)
     * @param status         상태 (all, processed, unprocessed)
     * @return 필터링된 신고 목록
     */
    List<Report> searchReports(String searchKeyword, String searchCategory, String dateType,
                               String startDate, String endDate, String status);

    boolean updateReportStatus(Long reportNo);

    Report registerReport(ReportRegisterRequestDto dto, String reporterMemberId);
}