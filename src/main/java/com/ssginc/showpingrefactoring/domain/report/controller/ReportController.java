package com.ssginc.showpingrefactoring.domain.report.controller;

import com.ssginc.showpingrefactoring.domain.report.dto.object.ReportDto;
import com.ssginc.showpingrefactoring.domain.report.dto.request.ReportRegisterRequestDto;
import com.ssginc.showpingrefactoring.domain.report.dto.response.ReportResponseDto;
import com.ssginc.showpingrefactoring.domain.report.entity.Report;
import com.ssginc.showpingrefactoring.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 신고 관리 요청-응답을 수행하는 컨트롤러 클래스
 * <p>
 */
@Tag(name = "Report", description = "신고 관리 API")
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 관리 페이지 렌더링 메소드
     * <p>
     * 요청 파라미터가 ReportDto 커맨드 객체에 바인딩되며,
     * 해당 검색 조건에 따라 신고 목록을 필터링하여 조회.
     * 조회된 신고 목록과 ReportDto 객체를 모델에 추가하여 뷰에 전달
     *
     * @param reportDto 검색 조건을 담은 ReportDto 객체
     * @param model     뷰에 데이터를 전달하기 위한 Model 객체
     * @return 신고 관리 페이지 뷰 이름 (report/report)
     */
    @GetMapping("/report")
    public String reportManagement(@ModelAttribute ReportDto reportDto, Model model, HttpServletRequest request) {
        List<Report> reports;
        if (hasSearchCriteria(reportDto)) {
            reports = reportService.searchReports(
                    reportDto.getSearchKeyword(),
                    reportDto.getSearchCategory(),
                    reportDto.getDateType(),
                    reportDto.getStartDate(),
                    reportDto.getEndDate(),
                    reportDto.getStatus()
            );
        } else {
            reports = reportService.getAllReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("reportDto", reportDto);

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "report/report :: reportTable";
        } else {
            return "report/report";
        }
    }

    // JSON API 엔드포인트
    @Operation(summary = "신고 목록 조회", description = "검색 조건에 따라 신고 목록 조회.")
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReportResponseDto.class)))
    @GetMapping("/list")
    @ResponseBody
    public List<ReportResponseDto> getReportList(
            @Parameter(description = "신고 검색 조건 (DTO)") @ModelAttribute ReportDto reportDto) {

        List<Report> reports;
        if (hasSearchCriteria(reportDto)) {
            reports = reportService.searchReports(
                    reportDto.getSearchKeyword(),
                    reportDto.getSearchCategory(),
                    reportDto.getDateType(),
                    reportDto.getStartDate(),
                    reportDto.getEndDate(),
                    reportDto.getStatus()
            );
        } else {
            reports = reportService.getAllReports();
        }

        return reports.stream().map(report -> {
            String formattedDate = "";
            if (report.getReportCreatedAt() != null) {
                formattedDate = report.getReportCreatedAt().toString();
            }
            return new ReportResponseDto(
                    report.getReportNo(),
                    formattedDate,
                    report.getReportReason(),
                    report.getReportContent(),
                    report.getMember().getMemberId(),
                    report.getReportStatus().getReportStatus()
            );
        }).collect(Collectors.toList());
    }

    @Operation(summary = "신고 상태 업데이트", description = "신고 번호를 기반으로 상태를 업데이트.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "업데이트 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/updateStatus")
    public ResponseEntity<?> updateReportStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "신고 번호", required = true,
                    content = @Content(schema = @Schema(example = "{\"reportNo\": 1234}")))
            @RequestBody Map<String, Object> payload) {
        try {
            Long reportNo = Long.valueOf(payload.get("reportNo").toString());
            boolean updated = reportService.updateReportStatus(reportNo);
            if (updated) {
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Update failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "신고 등록", description = "신고 정보를 등록. 인증된 사용자만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerReport(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "신고 등록 요청 DTO", required = true)
            @RequestBody ReportRegisterRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            Report newReport = reportService.registerReport(dto, userDetails.getUsername());
            return ResponseEntity.ok("Report registered with id: " + newReport.getReportNo());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * ReportDto에 검색 조건이 하나라도 존재하는지 여부를 판단.
     *
     * @param reportDto 검색 조건을 담은 ReportDto 객체
     * @return 검색 조건이 존재하면 true, 없으면 false
     */
    private boolean hasSearchCriteria(ReportDto reportDto) {
        return (reportDto.getSearchKeyword() != null && !reportDto.getSearchKeyword().trim().isEmpty())
                || (reportDto.getStartDate() != null && !reportDto.getStartDate().trim().isEmpty())
                || (reportDto.getEndDate() != null && !reportDto.getEndDate().trim().isEmpty())
                || (reportDto.getDateType() != null && !reportDto.getDateType().trim().isEmpty())
                || (reportDto.getStatus() != null && !reportDto.getStatus().trim().isEmpty() && !reportDto.getStatus().equals("all"));
    }
}