package com.ssginc.showpingrefactoring.domain.stream.controller;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.service.SubtitleService;
import com.ssginc.showpingrefactoring.domain.stream.service.VodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dckat
 * VOD 관련 요청-응답을 수행하는 컨트롤러 클래스
 * <p>
 */
@Tag(name = "vod", description = "VOD 관련 API")
@Controller
@RequestMapping("/api/vod")
@RequiredArgsConstructor
public class VodApiController {

    private final VodService vodService;

    private final SubtitleService subtitleService;

    /**
     * 전체 Vod 목록을 반환해주는 컨트롤러 메서드
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list")
    @Operation(
            summary = "VOD 목록 조회",
            description = "등록된 모든 VOD 정보를 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = StreamResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<?> getVodList() {
        List<StreamResponseDto> vodList = vodService.getAllVod();

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("vodList", vodList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 전체 Vod 목록을 반환해주는 컨트롤러 메서드
     * @param pageNo 요청한 페이지 번호
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list/page")
    @Operation(
            summary = "VOD 목록 조회 페이지네이션",
            description = "등록된 VOD 정보를 페이지네이션하여 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(
            name        = "pageNo",
            in          = ParameterIn.QUERY,
            description = "요청할 페이지 번호 (0부터 시작)",
            required    = false,
            schema      = @Schema(type = "integer", defaultValue = "0", minimum = "0")
    )
    public ResponseEntity<?> getVodListByPage(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo) {
        // 페이지 당 불러올 객체 단위 지정 (4개)
        Pageable pageable = PageRequest.of(pageNo, 4);
        Page<StreamResponseDto> pageInfo = vodService.getAllVodByPage(pageable);

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("pageInfo", pageInfo);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 영상 조회수를 기준으로 내림차순 페이지네이션 반환해주는 컨트롤러 메서드
     * @param pageNo 요청한 페이지 번호
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list/watch")
    @Operation(
            summary = "VOD 목록 페이지네이션 (시청수 기준 내림차순)",
            description = "등록된 VOD 정보를 시청수 기준으로 내림차순 정렬하여 페이지네이션 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(
            name        = "pageNo",
            in          = ParameterIn.QUERY,
            description = "요청할 페이지 번호 (0부터 시작)",
            required    = false,
            schema      = @Schema(type = "integer", defaultValue = "0", minimum = "0")
    )
    public ResponseEntity<?> getVodListByWatch(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo) {
        // 페이지 당 불러올 객체 단위 지정 (4개)
        Pageable pageable = PageRequest.of(pageNo, 4);
        Page<StreamResponseDto> pageInfo = vodService.getAllVodByWatch(pageable);

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("pageInfo", pageInfo);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 전체 Vod 목록을 카테고리에 따라 반환해주는 컨트롤러 메서드
     * @param categoryNo 카테고리 번호
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list/{categoryNo}")
    @Operation(
            summary = "카테고리 별 VOD 정보 조회",
            description = "등록된 VOD 정보를 카테고리별로 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = StreamResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(name = "categoryNo", description = "카테고리 번호", example = "0")
    public ResponseEntity<?> getVodListByCategory(@PathVariable Long categoryNo) {
        List<StreamResponseDto> vodList = vodService.getAllVodByCategory(categoryNo);
        Map<String, Object> result = new HashMap<>();

        result.put("vodList", vodList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 카테고리별 Vod 목록을 페이지네이션하여 반환해주는 컨트롤러 메서드
     * @param categoryNo 카테고리 번호
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list/category")
    @Operation(
            summary = "카테고리별 VOD 목록 페이지네이션",
            description = "등록된 VOD 정보를 카테고리별 페이지네이션하여 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    public ResponseEntity<?> getVodListByCategoryAndPage(@RequestParam(name = "categoryNo") Long categoryNo,
                                                         @RequestParam(defaultValue = "0", name = "pageNo") int pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 4);
        Page<StreamResponseDto> pageInfo = vodService.getAllVodByCategoryAndPage(categoryNo, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("pageInfo", pageInfo);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 영상 조회수를 기준으로 내림차순 페이지네이션 반환해주는 컨트롤러 메서드
     * @param pageNo 요청한 페이지 번호
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list/category-watch")
    @Operation(
            summary = "카테고리별 VOD 목록 시청 수 순으로 페이지네이션",
            description = "카테고리별 VOD 목록을 시청 수 순으로 페이지네이션하여 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    public ResponseEntity<?> getVodListByCategoryAndWatch(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo,
                                                          @RequestParam(name = "categoryNo") Long categoryNo) {
        // 페이지 당 불러올 객체 단위 지정 (4개)
        Pageable pageable = PageRequest.of(pageNo, 4);
        Page<StreamResponseDto> pageInfo = vodService.getAllVodByCatgoryAndWatch(categoryNo, pageable);

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("pageInfo", pageInfo);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * VOD 파일을 NCP Storage에 저장을 요청하는 컨트롤러 메서드
     * @param requestData 요청 데이터 정보
     * @return VOD의 저장결과 응답객체
     */
    @Operation(
            summary     = "VOD 업로드",
            description = "제목(title)을 전달받아 VOD를 업로드합니다."
    )
    @ApiResponses({
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
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    public ResponseEntity<?> uploadVod(@RequestBody Map<String, String> requestData) {
        String title = requestData.get("title");
        return ResponseEntity.ok(vodService.uploadVideo(title));
    }

    /**
     * 파일 제목으로 자막 정보 파일을 가져오는 메서드
     * @param title 파일 제목
     * @return 자막 생성 여부 응답 객체
     */
    @GetMapping("/subtitle/{title}.json")
    @Operation(
            summary     = "자막 조회",
            description = "요청한 파일 제목에 해당하는 자막 정보를 JSON 형태로 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", description = "자막 정보 JSON")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(
            name        = "title",
            in          = ParameterIn.PATH,
            description = "조회할 자막 파일의 제목 (확장자 제외)",
            required    = true,
            example     = "노트북_특가"
    )
    public ResponseEntity<?> getSubtitle(@PathVariable String title) {
        Resource subtitleJson = subtitleService.getSubtitle(title);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subtitleJson);
    }

}
