package com.ssginc.showpingrefactoring.infrastructure.ffmpeg;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class HlsMaker {
    public static String resolveFFmpegPath() {

        // 1) 존재하면 그대로 사용
        if (isOnPath("ffmpeg")) return "ffmpeg";

        // 2) OS별 기본 경로 후보(예시). 환경마다 바꿔 쓰세요.
        String os = System.getProperty("os.name").toLowerCase();
        List<String> candidates = getStrings(os);

        return candidates.stream()
                .filter(p -> Files.isRegularFile(Path.of(p)))
                .findFirst()
                .orElse("ffmpeg"); // 마지막 fallback
    }

    private static List<String> getStrings(String os) {
        List<String> candidates = new ArrayList<>();

        if (os.contains("win")) {
            candidates.add("C:\\ffmpeg\\bin\\ffmpeg.exe");
        } else if (os.contains("mac")) {
            candidates.add("/opt/homebrew/bin/ffmpeg");   // Apple Silicon(Homebrew)
            candidates.add("/usr/local/bin/ffmpeg");      // Intel(Homebrew)
            candidates.add("/opt/local/bin/ffmpeg");      // MacPorts
        } else { // linux 등
            candidates.add("/usr/bin/ffmpeg");
            candidates.add("/usr/local/bin/ffmpeg");
        }
        return candidates;
    }

    private static boolean isOnPath(String cmd) {
        try {
            Process p = new ProcessBuilder(cmd, "-version")
                    .redirectErrorStream(true).start();
            return p.waitFor() == 0;
        } catch (Exception ignore) { return false; }
    }
}
