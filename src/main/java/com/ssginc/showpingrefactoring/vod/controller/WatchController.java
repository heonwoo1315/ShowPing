package com.ssginc.showpingrefactoring.vod.controller;

import com.ssginc.showpingrefactoring.vod.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.vod.dto.response.VodResponseDto;
import com.ssginc.showpingrefactoring.vod.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.vod.service.VodService;
import com.ssginc.showpingrefactoring.vod.service.WatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dckat
 * 영상 시청과 관련한 요청-응답을 수행하는 컨트롤러 클래스
 * <p>
 */
@Controller
@RequestMapping("/api/watch")
@RequiredArgsConstructor
public class WatchController {

    private final StreamService streamService;

    private final VodService vodService;

    private final WatchService watchService;

    private final MemberService memberService;

    /**
     * VOD 페이지 이동을 위한 컨트롤러 메서드
     * @param userDetails 로그인한 사용자 객체
     * @param streamNo 시청할 영상 번호
     * @param model 타임리프에 전달할 Model 객체
     * @return 라이브 메인 페이지 (타임리프)
     */
    @GetMapping("/vod/{streamNo}")
    public String watchVod(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long streamNo,
                           Model model) {
        // 로그인 여부 확인
        if (userDetails != null) {
            Member member = memberService.findMemberById(userDetails.getUsername());
            model.addAttribute("member", member);
        }
        else {
            model.addAttribute("member", new Member());
        }

        GetStreamProductInfoResponseDto streamProductInfo = streamService.getStreamProductInfo(streamNo);

        // VOD 객체 정보 불러오기
        VodResponseDto vodDto = vodService.getVodByNo(streamNo);
        model.addAttribute("vodDto", vodDto);
        model.addAttribute("productInfo", streamProductInfo);

        return "watch/vod";
    }

    /**
     * 로그인한 사용자 시청내역 페이지 메서드
     * @return 사용자 시청내역 페이지 (타임리프)
     */
    @GetMapping("/history")
    public String watchHistory() {
        return "watch/history";
    }

    /**
     * 로그인한 사용자의 시청내역 리스트를 반환하는 컨트롤러 메서드
     * @param userDetails 로그인한 사용자
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @GetMapping("/history/list")
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
    public ResponseEntity<?> insertWatchHistory(@AuthenticationPrincipal UserDetails userDetails, @RequestBody WatchRequestDto watchRequestDto) {
        Long memberNo = null;
        if (userDetails != null) {
            Member member = memberService.findMemberById(userDetails.getUsername());
            memberNo = member.getMemberNo();
        }
        Watch watch = watchService.insertWatchHistory(watchRequestDto, memberNo);
        return ResponseEntity.ok(watch);
    }

}