package com.ssginc.showpingrefactoring.domain.stream.service.implement;

import com.ssginc.showpingrefactoring.infrastructure.NCP.storage.StorageLoader;
import com.ssginc.showpingrefactoring.domain.stream.service.HlsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author dckat
 * HLS과 관련한 로직을 구현한 서비스 클래스
 * <p>
 */
@Service
@RequiredArgsConstructor
public class HlsServiceImpl implements HlsService {

    @Value("${download.path}")
    private String VIDEO_PATH;

    @Qualifier("webApplicationContext")
    private final ResourceLoader resourceLoader;

    private final StorageLoader storageLoader;

    /**
     * 영상 제목으로 HLS 생성하여 받아오는 메서드
     * @param title 영상 제목
     * @return HLS 파일 (확장자: m3u8)
     */
    @Override
    public Mono<?> getHLSV1(String title) {
        return Mono.fromCallable(() -> {
            File inputFile = new File(VIDEO_PATH, title + ".mp4");
            File outputFile = new File(VIDEO_PATH, title + ".m3u8");

            // FFmpeg를 사용하여 HLS로 변환
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg", "-i", inputFile.getAbsolutePath(),
                    "-codec:", "copy", "-start_number", "0",
                    "-hls_time", "10", "-hls_list_size", "0",
                    "-f", "hls", outputFile.getAbsolutePath()
            );
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 변환 실패. Exit code: " + exitCode);
            }

            // 변환된 m3u8 파일을 Resource로 반환
            return resourceLoader.getResource("file:" + outputFile.getAbsolutePath());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 영상 제목과 segment 번호로 TS 파일을 받아오는 메서드
     * @param title 영상 제목
     * @param segment 세그먼트 번호
     * @return TS 파일 (확장자: ts)
     */
    @Override
    public Mono<?> getTsSegmentV1(String title, String segment) {
        return Mono.fromCallable(() ->
                resourceLoader.getResource("file:" + VIDEO_PATH + title + segment + ".ts"));
    }

    /**
     * HLS를 생성하여 NCP에 저장하는 메서드
     * @param title 영상 제목
     * @return HLS 파일 (확장자: m3u8)
     */
    @Override
    public String createHLS(String title) throws IOException, InterruptedException {
        String dirStr = VIDEO_PATH + "hls";
        File inputFile = new File(VIDEO_PATH, title + ".mp4");
        File outputFile = new File(dirStr, title + ".m3u8");
        File outputDir = new File(dirStr);


        // FFmpeg를 사용하여 HLS로 변환
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-i", inputFile.getAbsolutePath(),
                "-codec:", "copy", "-start_number", "0",
                "-hls_time", "10", "-hls_list_size", "0",
                "-f", "hls", outputFile.getAbsolutePath()
        );
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 변환 실패. Exit code: " + exitCode);
        }

        File[] files = outputDir.listFiles();

        return storageLoader.uploadHlsFiles(files, title);
    }

    /**
     * NCP Storage에 저장된 HLS를 불러오는 메서드
     * @param title 파일 제목
     * @return HLS 파일 (확장자: m3u8)
     */
    @Override
    public Mono<?> getHLSV2Flux(String title) {
        return Mono.fromCallable(() -> {
                    String fileName = title + ".m3u8";
                    return storageLoader.getHLS(fileName);
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(FileNotFoundException.class, e -> Mono.empty());
    }

    /**
     * 영상 제목과 segment 번호로 NCP Storage에 저장된 TS 파일을 받아오는 메서드
     * @param title 영상 제목
     * @param segment 세그먼트 번호
     * @return TS 파일 (확장자: ts)
     */
    @Override
    public Mono<?> getTsSegmentV2Flux(String title, String segment) {
        return Mono.fromCallable(() -> {
                    String fileName = title + segment + ".ts";
                    return storageLoader.getHLS(fileName);
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(FileNotFoundException.class, e -> Mono.empty());
    }

    /**
     * NCP Storage에 저장된 HLS를 불러오는 메서드 (동기 방식)
     * @param title 파일 제목
     * @return HLS 파일 (확장자: m3u8) 또는 파일을 찾을 수 없으면 null
     */
    @Override
    public Resource getHLSV2(String title) {
        String fileName = title + ".m3u8";
        return storageLoader.getHLS(fileName);
    }

    /**
     * 영상 제목과 segment 번호로 NCP Storage에 저장된 TS 파일을 받아오는 메서드 (동기 방식)
     * @param title 영상 제목
     * @param segment 세그먼트 번호
     * @return TS 파일 (확장자: ts) 또는 파일을 찾을 수 없으면 null
     */
    @Override
    public Resource getTsSegmentV2(String title, String segment) {
        String fileName = title + segment + ".ts";
        return storageLoader.getHLS(fileName);
    }

}