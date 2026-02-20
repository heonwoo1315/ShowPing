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
        // 1. hls 하위 폴더 경로 설정 및 생성
        File hlsFolder = new File(VIDEO_PATH, "hls");
        if (!hlsFolder.exists()) {
            hlsFolder.mkdirs();
        }

        // 2. 파일 경로 설정 (출력은 hls 폴더 안으로)
        File inputFile = new File(VIDEO_PATH, title + ".mp4");
        File outputFile = new File(hlsFolder, title + ".m3u8");

        // 3. FFmpeg 명령어 실행 (재인코딩 포함)
        List<String> cmd = Arrays.asList(
                "ffmpeg", "-y", "-i", inputFile.getAbsolutePath(),
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
        });
        gobbler.start();
        int exitCode = p.waitFor();
        gobbler.join();

        if (exitCode != 0) {
            throw new CustomException(ErrorCode.HLS_CONVERSION_FAILED);
        }

        // 4. NCP Storage 업로드 (hls 폴더 내의 파일들만)
        File[] hlsFiles = hlsFolder.listFiles((dir, name) -> name.startsWith(title));
        if (hlsFiles != null && hlsFiles.length > 0) {
            storageLoader.uploadHlsFiles(hlsFiles, title);
        }

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