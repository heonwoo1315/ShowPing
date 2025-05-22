package com.ssginc.showpingrefactoring.domain.stream.swagger;

import com.ssginc.showpingrefactoring.common.swagger.ApiResponseDescriptions;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.ssginc.showpingrefactoring.common.swagger.ApiResponseDescriptions.SERVER_ERROR;

@Tag(name = "vod", description = "VOD 관련 API")
@ApiResponse(responseCode = "500", description = SERVER_ERROR)
public interface VodApiSpecification {

    @Operation(
            summary = "VOD 목록 조회",
            description = "등록된 모든 VOD 정보를 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = StreamResponseDto.class)
                    )
            )
    )
    ResponseEntity<?> getVodList();


    @Operation(
            summary = "VOD 목록 조회 페이지네이션",
            description = "등록된 VOD 정보를 페이지네이션하여 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    @Parameter(
            name        = "pageNo",
            in          = ParameterIn.QUERY,
            description = "요청할 페이지 번호 (0부터 시작)",
            required    = false,
            schema      = @Schema(type = "integer", defaultValue = "0", minimum = "0")
    )
    ResponseEntity<?> getVodListByPage(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo);

    @Operation(
            summary = "VOD 목록 페이지네이션 (시청수 기준 내림차순)",
            description = "등록된 VOD 정보를 시청수 기준으로 내림차순 정렬하여 페이지네이션 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    @Parameter(
            name        = "pageNo",
            in          = ParameterIn.QUERY,
            description = "요청할 페이지 번호 (0부터 시작)",
            required    = false,
            schema      = @Schema(type = "integer", defaultValue = "0", minimum = "0")
    )
    ResponseEntity<?> getVodListByWatch(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo);

    @Operation(
            summary = "카테고리 별 VOD 정보 조회",
            description = "등록된 VOD 정보를 카테고리별로 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = StreamResponseDto.class)
                    )
            )
    )
    @Parameter(name = "categoryNo", description = "카테고리 번호", example = "0")
    ResponseEntity<?> getVodListByCategory(@PathVariable Long categoryNo);

    @Operation(
            summary = "카테고리별 VOD 목록 페이지네이션",
            description = "등록된 VOD 정보를 카테고리별 페이지네이션하여 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    @Parameters({
            @Parameter(
                    name        = "categoryNo",
                    in          = ParameterIn.QUERY,
                    description = "카테고리 번호",
                    required    = true,
                    schema      = @Schema(type = "long", defaultValue = "0", minimum = "0")
            ),
            @Parameter(
                    name        = "pageNo",
                    in          = ParameterIn.QUERY,
                    description = "요청할 페이지 번호 (0부터 시작)",
                    required    = false,
                    schema      = @Schema(type = "integer", defaultValue = "0", minimum = "0")
            )
    })
    ResponseEntity<?> getVodListByCategoryAndPage(@RequestParam(name = "categoryNo") Long categoryNo,
                                                  @RequestParam(defaultValue = "0", name = "pageNo") int pageNo);

    @Operation(
            summary = "카테고리별 VOD 목록 시청 수 순으로 페이지네이션",
            description = "카테고리별 VOD 목록을 시청 수 순으로 페이지네이션하여 조회"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    @Parameters({
            @Parameter(
                    name        = "pageNo",
                    in          = ParameterIn.QUERY,
                    description = "요청할 페이지 번호 (0부터 시작)",
                    required    = false,
                    schema      = @Schema(type = "integer", defaultValue = "0", minimum = "0")
            ),
            @Parameter(
                    name        = "categoryNo",
                    in          = ParameterIn.QUERY,
                    description = "카테고리 번호",
                    required    = true,
                    schema      = @Schema(type = "long", defaultValue = "0", minimum = "0")
            )
    })
    ResponseEntity<?> getVodListByCategoryAndWatch(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo,
                                                   @RequestParam(name = "categoryNo") Long categoryNo);


    @Operation(
            summary     = "VOD 업로드",
            description = "제목(title)을 전달받아 VOD를 업로드합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "업로드 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            type        = "String",
                            description = "업로드 처리 결과",
                            example     = "title"
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "업로드할 VOD 제목을 담은 JSON",
            required    = true,
            content     = @Content(
                    mediaType = "application/json",
                    schema    = @Schema(
                            type        = "object",
                            description = "요청 파일 예시",
                            example     = "{\"title\":\"노트북_특가\"}"
                    )
            )
    )
    @PostMapping("/upload")
    ResponseEntity<?> uploadVod(@RequestBody Map<String, String> requestData);

    @Operation(
            summary     = "자막 조회",
            description = "요청한 파일 제목에 해당하는 자막 정보를 JSON 형태로 반환"
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", description = "자막 정보 JSON")
            )
    )
    @Parameter(
            name        = "title",
            in          = ParameterIn.PATH,
            description = "조회할 자막 파일의 제목 (확장자 제외)",
            required    = true,
            example     = "노트북_특가"
    )
    ResponseEntity<?> getSubtitle(@PathVariable String title);
}
