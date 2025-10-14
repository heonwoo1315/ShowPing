package com.ssginc.showpingrefactoring.domain.watch.swagger;

import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchHistoryListRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import static com.ssginc.showpingrefactoring.common.swagger.ApiResponseDescriptions.SERVER_ERROR;

@Tag(name = "watch", description = "시청 관련 API")
@ApiResponse(responseCode = "500", description = SERVER_ERROR)
public interface WatchApiSpecification {

    @Operation(
            summary = "시청 내역 조회",
            description = "로그인한 회원의 시청내역 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = WatchResponseDto.class)
                    )
            )
    )
    @Parameter(hidden = true)
    ResponseEntity<?> getWatchHistory(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(
            summary = "시청 내역 조회 페이지네이션",
            description = "로그인한 회원의 시청내역 조회 페이지네이션"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = WatchResponseDto.class)
                    )
            )
    )
    @Parameter(hidden = true)
    ResponseEntity<?> getWatchHistoryPage(@AuthenticationPrincipal UserDetails userDetails,
                                          @ModelAttribute @Valid WatchHistoryListRequestDto watchHistoryListRequestDto);


    @Operation(
            summary = "시청 내역 추가",
            description = "라이브 및 VOD 시청이력 추가"
    )
    @ApiResponse(
            responseCode = "200",
            description = "시청내역 추가 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = WatchResponseDto.class)
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "추가할 시청 정보를 담은 JSON",
            required    = true,
            content     = @Content(
                    mediaType = "application/json",
                    schema    = @Schema(
                            type        = "object",
                            description = "추가할 시청 정보 예시",
                            example     = "{\"streamNo\":\"1\", \"watchTime\":\"2025-01-01T12:00:00\"}"
                    )
            )
    )
    ResponseEntity<?> insertWatchHistory(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody WatchRequestDto watchRequestDto);

}
