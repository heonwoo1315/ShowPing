package com.ssginc.showpingrefactoring.domain.stream.service.implement;

import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
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

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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
                throw new CustomException(ErrorCode.HLS_CONVERSION_FAILED);
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
     * 디렉토리(및 하위 파일들) 안전 삭제 유틸.
     */
    private void safeDeleteDirectory(Path dir) {
        if (dir == null) return;
        try {
            if (Files.exists(dir)) {
                // 파일 -> 디렉토리 역순으로 삭제
                try (Stream<Path> walk = Files.walk(dir)) {
                    walk.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                            });
                }
            }
            // 상위 hls 폴더가 비면(옵션) 정리하고 싶다면 아래 주석 해제
            // Path parent = dir.getParent();
            // if (parent != null && Files.isDirectory(parent) && isEmptyDirectory(parent)) {
            //     Files.deleteIfExists(parent);
            // }
        } catch (IOException ignored) {
            // TODO: 필요시 로깅
            // log.warn("Failed to cleanup HLS dir: {}", dir, ignored);
        }
    }

    // (옵션) 디렉토리 비었는지 검사 유틸
    @SuppressWarnings("unused")
    private boolean isEmptyDirectory(Path dir) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            return !ds.iterator().hasNext();
        }
    }

    /**
     * HLS를 생성하여 NCP에 저장하는 메서드
     * @param title 영상 제목
     * @return HLS 파일 (확장자: m3u8)
     */
    @Override
    public String createHLS(String title) throws IOException, InterruptedException {
        // Nginx 설정과 맞추기 위해 hls 하위 폴더 대신 VIDEO_PATH에 바로 생성
        // Nginx는 /api/hls/v2/ 요청을 /home/ec2-user/video/ 에 찾고 있음

        // 2. 파일 경로 설정
        File inputFile = new File(VIDEO_PATH, title + ".mp4");
        File outputFile = new File(VIDEO_PATH, title + ".m3u8");

        // 3. FFmpeg 명령어 실행 (재인코딩 옵션 포함으로 인한 타임 스탬프 오류 해결)
        List<String> cmd = Arrays.asList(
                "ffmpeg", "-y",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "libx264", "-c:a", "aac", "-b:a", "128k",
                "-start_number", "0", "-hls_time", "10", "-hls_list_size", "0",
                "-f", "hls",
                outputFile.getAbsolutePath()
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // 로그 소비 쓰레드
        Thread gobbler = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while (br.readLine() != null) { }
            } catch (IOException ignored) {}
        }, "ffmpeg-output-gobbler");
        gobbler.start();

        int exitCode = p.waitFor();
        gobbler.join();

        if (exitCode != 0) {
            throw new CustomException(ErrorCode.HLS_CONVERSION_FAILED);
        }

        // NCP 업로드는 수행하되, 로컬 파일은 지우지 않는다.
        // 현재 Nginx가 로컬 디스크의 파일을 직접 읽어 서빙하도록 설정되어 있기 때문
        File outputDir = new File(VIDEO_PATH);
        File[] files = outputDir.listFiles((dir, name) -> name.startsWith(title));

        if (files != null && files.length > 0) {
            storageLoader.uploadHlsFiles(files, title);
        }

        // safeDeleteDirectory(outputDir.toPath()); // 이 줄을 주석 처리하여 파일을 남겨둔다

        return "SUCCESS";
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