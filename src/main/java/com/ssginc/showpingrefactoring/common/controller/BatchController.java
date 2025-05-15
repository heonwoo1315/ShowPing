package com.ssginc.showpingrefactoring.common.controller;

import com.ssginc.showpingrefactoring.domain.stream.dto.request.VodTitleRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dckat
 * 배치 작업을 처리하는 컨트롤러 클래스
 * <p>
 */
@Tag(name = "batch", description = "Batch 작업 수행")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job createHlsJob;
    private final Job createSubtitleJob;

    /**
     * HLS 저장 작업을 실행하는 컨트롤러 메서드
     * @param vodTitleRequestDto 파일 요청 DTO (파일 제목 포함)
     * @return 작업 실행 ID를 포함한 ResponseEntity
     */
    @PostMapping("/hls/create")
    @Operation(
            summary = "HLS 생성 배치",
            description = "영상 저장 후 HLS 생성을 위한 배치작업 진행"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "HLS 생성을 위한 batch 작업 생성 완료"
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "영상 제목 정보 DTO",
            required    = true,
            content     = @Content(
                    mediaType = "application/json",
                    schema    = @Schema(
                            type        = "object",
                            description = "HLS 생성을 위한 영상 제목",
                            example     = "{\"fileTitle\":\"stream_01_노트북_특가.mp4\"}"
                    )
            )
    )
    public ResponseEntity<String> createHLS(@RequestBody VodTitleRequestDto vodTitleRequestDto) throws Exception {
        String title = vodTitleRequestDto.getFileTitle();
        JobParameters params = new JobParametersBuilder()
                .addString("title", title)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution exec = jobLauncher.run(createHlsJob, params);
        return ResponseEntity.accepted()
                .body("saveHlsJob 실행 ID=" + exec.getId());
    }

    /**
     * 자막 생성 작업을 실행하는 컨트롤러 메서드
     * @param vodTitleRequestDto 파일 요청 DTO (파일 제목 포함)
     * @return 작업 실행 ID를 포함한 ResponseEntity
     */
    @PostMapping("/subtitle/create")
    @Operation(
            summary = "자막 생성 배치",
            description = "영상 저장 후 자막생성을 위한 배치작업 진행"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "자막 생성을 위한 batch 작업 생성 완료"
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "자막 생성을 위한 영상 제목 정보 DTO",
            required    = true,
            content     = @Content(
                    mediaType = "application/json",
                    schema    = @Schema(
                            type        = "object",
                            description = "자막 생성을 위한 영상 제목",
                            example     = "{\"fileTitle\":\"stream_01_노트북_특가.mp4\"}"
                    )
            )
    )
    public ResponseEntity<String> createSubtitle(@RequestBody VodTitleRequestDto vodTitleRequestDto) throws Exception {
        String title = vodTitleRequestDto.getFileTitle();
        JobParameters params = new JobParametersBuilder()
                .addString("title", title)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution exec = jobLauncher.run(createSubtitleJob, params);
        return ResponseEntity.accepted()
                .body("createSubtitleJob 실행 ID=" + exec.getId());
    }

}
