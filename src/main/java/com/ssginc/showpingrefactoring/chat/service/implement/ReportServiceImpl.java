package com.ssginc.showpingrefactoring.chat.service.implement;

import com.ssginc.showpingrefactoring.chat.dto.request.ReportRegisterRequestDto;
import com.ssginc.showpingrefactoring.domain.chat.entity.Member;
import com.ssginc.showpingrefactoring.domain.chat.entity.Report;
import com.ssginc.showpingrefactoring.domain.chat.entity.ReportStatus;
import com.ssginc.showpingrefactoring.domain.chat.entity.ReportType;
import com.ssginc.showpingrefactoring.chat.repository.MemberRepository;
import com.ssginc.showpingrefactoring.chat.repository.ReportRepository;
import com.ssginc.showpingrefactoring.chat.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 신고 관리 구현체 클래스
 * <p>
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    /**
     * 전체 신고 목록을 반환.
     * * @return 전체 신고 리스트
     */
    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    /**
     * 검색 조건에 따른 신고 필터링 수행.
     * <p>
     * 주어진 검색어, 검색 카테고리, 날짜 범위, 및 상태를 기준으로 신고 목록 필터링.
     * * @param searchKeyword  검색어
     *
     * @param searchCategory 검색 카테고리 (reportNum, receipt, reason, writer, proceed)
     * @param dateType       날짜 유형 (접수일)
     * @param startDate      시작일 (yyyy-MM-dd)
     * @param endDate        종료일 (yyyy-MM-dd)
     * @param status         상태 (처리, 미처리)
     * @return 필터링된 신고 목록
     */
    @Override
    public List<Report> searchReports(String searchKeyword, String searchCategory, String dateType,
                                      String startDate, String endDate, String status) {
        String mappedStatus;
        if ("processed".equals(status)) {
            mappedStatus = "처리";
        } else if ("unprocessed".equals(status)) {
            mappedStatus = "미처리";
        } else {
            mappedStatus = "";
        }

        List<Report> reports = reportRepository.findAll();

        return reports.stream().filter(report -> {
            boolean matches = true;

            // 검색어 필터링
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                switch (searchCategory) {
                    case "reportNum":
                        matches = matches && report.getReportNo().toString().contains(searchKeyword);
                        break;
                    case "receipt":
                        matches = matches && report.getReportCreatedAt().toString().contains(searchKeyword);
                        break;
                    case "reason":
                        matches = matches && report.getReportReason().contains(searchKeyword);
                        break;
                    case "writer":
                        // member 엔티티의 식별값(예: memberId)을 기준으로 필터링
                        matches = matches && report.getMember().getMemberId().contains(searchKeyword);
                        break;
                    case "proceed":
                        matches = matches && report.getReportStatus().getReportStatus().contains(searchKeyword);
                        break;
                    default:
                        // 전체 검색 시 여러 필드를 체크
                        matches = matches && (
                                report.getReportNo().toString().contains(searchKeyword) ||
                                        report.getReportCreatedAt().toString().contains(searchKeyword) ||
                                        report.getReportReason().contains(searchKeyword) ||
                                        report.getMember().getMemberId().contains(searchKeyword) ||
                                        report.getReportStatus().getReportStatus().contains(searchKeyword)
                        );
                }
            }

            // 날짜 필터링 (현재는 접수일(reportCreatedAt)만 적용)
            if (dateType != null && !dateType.isEmpty() && startDate != null && !startDate.isEmpty()) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDateTime startDateTime = start.atStartOfDay();
                LocalDateTime endDateTime;
                if (endDate != null && !endDate.isEmpty()) {
                    LocalDate end = LocalDate.parse(endDate);
                    endDateTime = end.atTime(LocalTime.MAX);
                } else {
                    endDateTime = LocalDateTime.now();
                }
                if (dateType.equals("접수일")) {
                    matches = matches &&
                            (report.getReportCreatedAt().isEqual(startDateTime) || report.getReportCreatedAt().isAfter(startDateTime)) &&
                            (report.getReportCreatedAt().isEqual(endDateTime) || report.getReportCreatedAt().isBefore(endDateTime));
                }
            }

            // 상태 필터링
            if (mappedStatus != null && !mappedStatus.equals("all")) {
                if (mappedStatus.equals("처리")) {
                    matches = matches && report.getReportStatus() == ReportStatus.COMPLETED;
                } else if (mappedStatus.equals("미처리")) {
                    matches = matches && report.getReportStatus() == ReportStatus.PROCEEDING;
                }
            }

            return matches;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateReportStatus(Long reportNo) {
        // 해당 신고가 미처리(PROCEEDING)인 경우에만 업데이트
        Optional<Report> optReport = reportRepository.findById(reportNo);
        if (optReport.isPresent()) {
            Report report = optReport.get();
            if (ReportStatus.PROCEEDING.equals(report.getReportStatus())) {
                report.setReportStatus(ReportStatus.COMPLETED); // ENUM 변환: 미처리(PROCEEDING) -> 처리(COMPLETED)
                reportRepository.save(report);
                return true;
            }
        }
        return false;
    }

    @Override
    public Report registerReport(ReportRegisterRequestDto dto, String reporterMemberId) {
        // 신고한 회원 조회
        Member reporter = memberRepository.findByMemberId(reporterMemberId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        Report report = new Report();
        report.setMember(reporter);
        report.setReportStatus(ReportStatus.PROCEEDING); // 미처리 상태
        report.setReportType(ReportType.CHAT);           // 채팅 신고 타입
        report.setReportReason(dto.getReportReason());
        report.setReportContent(dto.getReportContent());
        report.setReportCreatedAt(LocalDateTime.now());  // 신고 시각

        return reportRepository.save(report);
    }
}