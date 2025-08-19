package com.ssginc.showpingrefactoring.domain.stream.swagger;

import com.ssginc.showpingrefactoring.common.dto.CustomErrorResponse;
import com.ssginc.showpingrefactoring.domain.product.dto.object.ProductItemDto;
import com.ssginc.showpingrefactoring.domain.product.dto.response.GetProductListResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.request.LiveRequestDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.request.RegisterLiveRequestDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.GetLiveRegisterInfoResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StartLiveResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Tag(name = "Live", description = "생방송 관련 API")
@ApiResponse(responseCode = "500", description = "서버 내부 오류",
        content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomErrorResponse.class),
                examples = {
                        @ExampleObject(
                                value = "{\n" +
                                        "  \"code\": \"INTERNAL_SERVER_ERROR\",\n" +
                                        "  \"message\": \"알 수 없는 서버 오류가 발생했습니다.\"\n" +
                                        "}"
                        )
                }
        )
)
public interface LiveApiSpecification {

    @GetMapping("/onair")
    @Operation(
            summary = "라이브 중인 방송 정보",
            description = "현재 라이브 중인 방송에 대한 정보를 가져옴."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "라이브 중인 방송 정보 가져오기 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StreamResponseDto.class)
                    )
            )
    })
    ResponseEntity<?> getOnair();


    @GetMapping("/active")
    @Operation(
            summary = "라이브 중 + 라이브 예정인 방송 정보 목록",
            description = "현재 라이브 중인 방송과 라이브 예정인 방송에 대한 정보 목록을 가져옴"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "페이징된 라이브 중 + 라이브 예정 방송 목록",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageStreamResponseDto.class)
                    )
            )
    })
    ResponseEntity<?> getActive(
            @Parameter(
                    description = "조회할 페이지 번호 (0부터 시작)",
                    schema = @Schema(type = "integer", defaultValue = "0", example = "0")
            )
            @RequestParam(defaultValue = "0", name = "pageNo") int pageNo);

    @GetMapping("/standby")
    @Operation(
            summary = "라이브 예정인 방송 정보 목록",
            description = "라이브 예정인 방송에 대한 정보 목록을 가져옴"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "페이징된 라이브 예정 방송 목록",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageStreamResponseDto.class)
                    )
            )
    })
    ResponseEntity<?> getStandby(
            @Parameter(
                    description = "조회할 페이지 번호 (0부터 시작)",
                    schema = @Schema(type = "integer", defaultValue = "0", example = "0")
            )
            @RequestParam(defaultValue = "0", name = "pageNo") int pageNo);

//    @GetMapping("/product/list")
//    @Operation(
//            summary = "방송 등록 중 상품 선택",
//            description = "방송 등록 페이지에서 상품 선택 시 상품 목록을 가져옴"
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "상품 목록 가져오기 성공",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            array = @ArraySchema(
//                                    schema = @Schema(implementation = ProductItemDto.class)
//                            )
//                    )
//            )
//    })
//    ResponseEntity<List<ProductItemDto>> getProductList();

//    @GetMapping("/product/list")
//    @Operation(
//            summary = "방송 등록 중 상품 선택",
//            description = "방송 등록 페이지에서 상품 선택 시 상품 목록을 가져옴, offset 방식의 Paging 적용"
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "상품 목록 가져오기 성공",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = PageProductItemDto.class)
//                    )
//            )
//    })
//    ResponseEntity<Page<ProductItemDto>> getProductList(
//            @Parameter(
//                    description = "조회할 페이지 번호 (0부터 시작)",
//                    schema = @Schema(type = "integer", defaultValue = "0", example = "0")
//            )
//            @RequestParam(defaultValue = "0") int page,
//            @Parameter(
//                    description = "페이지 당 조회할 개수",
//                    schema = @Schema(type = "integer", defaultValue = "20", example = "20")
//            )
//            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/prodcut/list")
    @Operation(
            summary = "방송 등록 중 상품 선택",
            description = "방송 등록 페이지에서 상품 선택 시 상품 목록을 가져옴, cursor 방식의 Paging 적용"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상품 목록 가져오기 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageProductItemDto.class)
                    )
            )
    })
    ResponseEntity<GetProductListResponseDto> getProductList(
            @Parameter(
                    description = "이전에 조회했던 페이지의 마지막 상품 번호",
                    schema = @Schema(type = "Long", example = "30")
            )
            @RequestParam(required = false) Long lastProductNo,
            @Parameter(
                    description = "페이지 당 조회할 개수",
                    schema = @Schema(type = "integer", defaultValue = "20", example = "20")
            )
            @RequestParam(defaultValue = "20") int size);

    @PostMapping("/register")
    @Operation(
            summary = "라이브 등록",
            description = "로그인된 사용자가 새 라이브를 등록하고, 생성된 라이브의 ID를 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "라이브 등록 성공 { \"streamNo\": 123 } 형태로 ID 반환",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"streamNo\": 123}"
                            )
                    )
            )
    })
    ResponseEntity<Map<String, Long>> resgisterLive(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등록할 라이브 정보",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterLiveRequestDto.class)
                    )
            )
            @RequestBody RegisterLiveRequestDto request);

    @PostMapping("/start")
    @Operation(
            summary = "방송 시작",
            description = "방송 시작 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "방송 시작 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StartLiveResponseDto.class)
                    )
            )
    })
    ResponseEntity<StartLiveResponseDto> startLive(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "시작할 방송의 방송 번호",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LiveRequestDto.class)
                    )
            )
            @RequestBody LiveRequestDto request);

    @PostMapping("/stop")
    @Operation(
            summary = "방송 중단",
            description = "방송 중단 API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "방송 중단 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"result\": true}"
                            )
                    )
            )
    })
    ResponseEntity<Map<String, Boolean>> stopLive(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "중단할 방송의 방송 번호",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LiveRequestDto.class)
                    )
            )
            @RequestBody LiveRequestDto request);

    @GetMapping("/live-info")
    @Operation(
            summary = "등록한 방송 정보",
            description = "등록해놓은 방송에 대한 정보를  가져옴"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "등록해놓은 방송 정보 가져오기 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetLiveRegisterInfoResponseDto.class)
                    )
            )
    })
    ResponseEntity<GetLiveRegisterInfoResponseDto> getLiveInfo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails);
}
