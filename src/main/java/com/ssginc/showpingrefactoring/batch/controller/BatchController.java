package com.ssginc.showpingrefactoring.batch.controller;

import com.ssginc.showpingrefactoring.vod.dto.request.FileRequestDto;
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
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job createHlsJob;
    private final Job createSubtitleJob;

    /**
     * HLS 저장 작업을 실행하는 컨트롤러 메서드
     * @param fileRequestDto 파일 요청 DTO (파일 제목 포함)
     * @return 작업 실행 ID를 포함한 ResponseEntity
     */
    @PostMapping("/hls/create")
    public ResponseEntity<String> createHLS(@RequestBody FileRequestDto fileRequestDto) throws Exception {
        String title = fileRequestDto.getFileTitle();
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
     * @param fileRequestDto 파일 요청 DTO (파일 제목 포함)
     * @return 작업 실행 ID를 포함한 ResponseEntity
     */
    @PostMapping("/subtitle/create")
    public ResponseEntity<String> createSubtitle(@RequestBody FileRequestDto fileRequestDto) throws Exception {
        String title = fileRequestDto.getFileTitle();
        JobParameters params = new JobParametersBuilder()
                .addString("title", title)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution exec = jobLauncher.run(createSubtitleJob, params);
        return ResponseEntity.accepted()
                .body("createSubtitleJob 실행 ID=" + exec.getId());
    }

}
