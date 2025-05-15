package com.ssginc.showpingrefactoring.domain.watch.controller;

import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.entity.Watch;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.service.WatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dckat
 * 영상 시청과 관련한 요청-응답을 수행하는 컨트롤러 클래스
 * <p>
 */
@Tag(name = "watch", description = "시청 관련 API")
@Controller
@RequestMapping("/api/watch")
@RequiredArgsConstructor
public class WatchApiController {

    private final WatchService watchService;

    private final MemberService memberService;

    /**
     * 로그인한 사용자의 시청내역 리스트를 반환하는 컨트롤러 메서드
     * @param userDetails 로그인한 사용자
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @GetMapping("/history/list")
    @Operation(
            summary = "시청 내역 조회",
            description = "로그인한 회원의 시청내역 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = WatchResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameter(hidden = true)
    public ResponseEntity<?> getWatchHistory(@AuthenticationPrincipal UserDetails userDetails) {
        // 로그인한 사용자의 정보를 가져오기
        Member member = memberService.findMemberById(userDetails.getUsername());
        Long memberNo = member.getMemberNo();
        List<WatchResponseDto> historyList = watchService.getWatchHistoryByMemberNo(memberNo);

        Map<String, Object> result = new HashMap<>();
        result.put("historyList", historyList);
        return ResponseEntity.ok(result);
    }

    /**
     * 시청 내역 등록 컨트롤러 메서드
     * @param watchRequestDto 시청내역 등록을 위한 요청 DTO (Body를 통해 전달)
     * @return 응답 결과
     */
    @PostMapping("/insert")
    @Operation(
            summary = "시청 내역 조회",
            description = "로그인한 회원의 시청내역 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = WatchResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    public ResponseEntity<?> insertWatchHistory(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestBody WatchRequestDto watchRequestDto) {
        Long memberNo = null;
        if (userDetails != null) {
            Member member = memberService.findMemberById(userDetails.getUsername());
            memberNo = member.getMemberNo();
        }
        Watch watch = watchService.insertWatchHistory(watchRequestDto, memberNo);
        return ResponseEntity.ok(watch);
    }

}