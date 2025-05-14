package com.ssginc.showpingrefactoring.domain.stream.controller;

import com.ssginc.showpingrefactoring.domain.stream.service.HlsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * @author dckat
 * HLS와 관련한 요청-응답을 수행하는 컨트롤러 클래스
 * <p>
 */
@Tag(name = "hls", description = "HLS 기반 VOD 재생")
@Controller
@RequestMapping("/api/hls")
@RequiredArgsConstructor
public class HlsController {

    private final HlsService hlsService;

    /**
     * 영상 제목으로 HLS 파일을 받아오는 컨트롤러 메서드
     * @param title 영상 제목
     * @return HLS 파일이 포함된 응답객체 (확장자: m3u8)
     */
    @GetMapping(value = "/v1/{title}.m3u8")
    @Operation(summary = "get M3U8 ver 1", description = "HLS metadata 요청 (생성 및 fetch)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/vnd.apple.mpegurl",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!")
    public Mono<?> getHLSV1(@PathVariable String title) {
        // 불러온 m3u8 파일을 응답으로 전송
        return hlsService.getHLSV1(title)
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.apple.mpegurl")
                        .body(resource));
    }

    /**
     * 영상 제목과 segment 번호로 TS 파일을 받아오는 컨트롤러 메서드
     * @param title 영상 제목
     * @param segment 세그먼트 번호
     * @return TS 파일이 있는 응답객체 (확장자: ts)
     */
    @GetMapping(value = "/v1/{title}{segment}.ts")
    @Operation(summary = "get TS ver 1", description = "10초 단위의 영상 segment 요청")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "video/mp2t",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters ({
            @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!"),
            @Parameter(name = "segment", description = "영상 segment 번호", example = "1")
    })
    public Mono<?> getTsSegmentV1(@PathVariable String title,
                                  @PathVariable String segment) {
        // 불러온 ts 파일을 응답으로 전송
        return hlsService.getTsSegmentV1(title, segment)
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                        .body(resource))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * 영상 제목과 segment 번호로 HLS 파일을 NCP Storage에서 받아오는 컨트롤러 메서드
     * @param title 영상 제목
     * @return TS 파일이 있는 응답객체 (확장자: ts)
     */
    @GetMapping(value = "/v2/flux/{title}.m3u8")
    @Operation(summary = "get M3U8 ver2 + WebFlux", description = "HLS metadata 요청 (fetch만 수행 + WebFlux)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/vnd.apple.mpegurl",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!")
    public Mono<?> getHLSV2Flux(@PathVariable String title) {
        // 불러온 ts 파일을 응답으로 전송
        return hlsService.getHLSV2Flux(title)
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                        .body(resource))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * 영상 제목과 segment 번호로 TS 파일을 NCP로 받아오는 컨트롤러 메서드
     * @param title 영상 제목
     * @param segment 세그먼트 번호
     * @return TS 파일이 있는 응답객체 (확장자: ts)
     */
    @GetMapping(value = "/v2/flux/{title}{segment}.ts")
    @Operation(summary = "get TS ver2 + WebFlux", description = "10초 단위의 영상 segment 요청 (WebFlux)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "video/mp2t",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters ({
            @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!"),
            @Parameter(name = "segment", description = "영상 segment 번호", example = "1")
    })
    public Mono<?> getTsSegmentV2Flux(@PathVariable String title,
                                      @PathVariable String segment) {
        // 불러온 ts 파일을 응답으로 전송
        return hlsService.getTsSegmentV2Flux(title, segment)
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                        .body(resource))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * 영상 제목으로 HLS 파일(m3u8)을 NCP Storage에서 받아오는 컨트롤러 메서드
     * @param title 영상 제목
     * @return TS 파일을 포함한 응답 객체 (확장자: ts)
     */
    @GetMapping(value = "/v2/{title}.m3u8")
    @Operation(summary = "get M3U8 ver2", description = "HLS metadata 요청 (fetch만 수행)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/vnd.apple.mpegurl",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!")
    public ResponseEntity<?> getHLSV2(@PathVariable String title) {
        // 동기 방식으로 리소스를 가져옵니다.
        Resource resource = hlsService.getHLSV2(title);
        if (resource != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 영상 제목과 segment 번호로 TS 파일을 NCP Storage에서 받아오는 컨트롤러 메서드
     * @param title 영상 제목
     * @param segment 세그먼트 번호
     * @return TS 파일을 포함한 응답 객체 (확장자: ts)
     */
    @GetMapping(value = "/v2/{title}{segment}.ts")
    @Operation(summary = "get TS ver2", description = "10초 단위의 영상 segment 요청")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "video/mp2t",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters ({
            @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!"),
            @Parameter(name = "segment", description = "영상 segment 번호", example = "1")
    })
    public ResponseEntity<?> getTsSegmentV2(@PathVariable String title,
                                            @PathVariable String segment) {
        // 동기 방식으로 리소스를 가져옵니다.
        Resource resource = hlsService.getTsSegmentV2(title, segment);
        if (resource != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
