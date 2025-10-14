package com.ssginc.showpingrefactoring.domain.stream.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

import static com.ssginc.showpingrefactoring.common.swagger.ApiResponseDescriptions.SERVER_ERROR;

@Tag(name = "hls", description = "HLS 기반 VOD 재생")
@ApiResponse(responseCode = "500", description = SERVER_ERROR)
public interface HlsSpecification {

    @Operation(summary = "get M3U8 ver 1", description = "HLS metadata 요청 (생성 및 fetch)")
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/vnd.apple.mpegurl",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!")
    Mono<?> getHLSV1(@PathVariable String title);

    @Operation(summary = "get TS ver 1", description = "10초 단위의 영상 segment 요청")
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "video/mp2t",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @Parameters({
            @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!"),
            @Parameter(name = "segment", description = "영상 segment 번호", example = "1")
    })
    Mono<?> getTsSegmentV1(@PathVariable String title,
                           @PathVariable String segment);

    @Operation(summary = "get M3U8 ver2 + WebFlux", description = "HLS metadata 요청 (fetch만 수행 + WebFlux)")
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/vnd.apple.mpegurl",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!")
    Mono<?> getHLSV2Flux(@PathVariable String title);

    @Operation(summary = "get TS ver2 + WebFlux", description = "10초 단위의 영상 segment 요청 (WebFlux)")
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "video/mp2t",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @Parameters ({
            @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!"),
            @Parameter(name = "segment", description = "영상 segment 번호", example = "1")
    })
    Mono<?> getTsSegmentV2Flux(@PathVariable String title,
                               @PathVariable String segment);

    @Operation(summary = "get M3U8 ver2", description = "HLS metadata 요청 (fetch만 수행)")
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/vnd.apple.mpegurl",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!")
    ResponseEntity<?> getHLSV2(@PathVariable String title);

    @Operation(summary = "get TS ver2", description = "10초 단위의 영상 segment 요청")
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "video/mp2t",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @Parameters ({
            @Parameter(name = "title", description = "영상 제목", example = "노트북 특가!"),
            @Parameter(name = "segment", description = "영상 segment 번호", example = "1")
    })
    ResponseEntity<?> getTsSegmentV2(@PathVariable String title,
                                     @PathVariable String segment);

}
