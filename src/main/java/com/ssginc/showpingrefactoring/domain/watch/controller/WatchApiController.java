package com.ssginc.showpingrefactoring.domain.watch.controller;

import com.ssginc.showpingrefactoring.common.dto.PageResponseDto;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchHistoryListRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.entity.Watch;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.service.WatchService;
import com.ssginc.showpingrefactoring.domain.watch.swagger.WatchApiSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@Controller
@RequestMapping("/api/watch")
@RequiredArgsConstructor
public class WatchApiController implements WatchApiSpecification {

    private final WatchService watchService;

    private final MemberService memberService;

    /**
     * 로그인한 사용자의 시청내역 리스트를 반환하는 컨트롤러 메서드
     * @param userDetails 로그인한 사용자
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Override
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

    @Override
    @GetMapping("/history/list/page")
    public ResponseEntity<?> getWatchHistoryPageV1(@AuthenticationPrincipal UserDetails userDetails,
                                                   @ModelAttribute @Valid WatchHistoryListRequestDto watchHistoryListRequestDto) {
        Member member = memberService.findMemberById(userDetails.getUsername());
        Long memberNo = member.getMemberNo();

        Pageable pageable = PageRequest.of(
                watchHistoryListRequestDto.getPageNo(),
                watchHistoryListRequestDto.getPageSize());

        Page<WatchResponseDto> pageResult = watchService.getWatchHistoryPage(memberNo,
                watchHistoryListRequestDto.getFromDate(),
                watchHistoryListRequestDto.getToDate(),
                pageable);
        return ResponseEntity.ok(PageResponseDto.of(pageResult));
    }

    /**
     * 시청 내역 등록 컨트롤러 메서드
     * @param watchRequestDto 시청내역 등록을 위한 요청 DTO (Body를 통해 전달)
     * @return 응답 결과
     */
    @Override
    @PostMapping("/insert")
    public ResponseEntity<?> insertWatchHistory(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestBody @Valid WatchRequestDto watchRequestDto) {
        Long memberNo = null;
        if (userDetails != null) {
            Member member = memberService.findMemberById(userDetails.getUsername());
            memberNo = member.getMemberNo();
        }
        Watch watch = watchService.insertWatchHistory(watchRequestDto, memberNo);
        return ResponseEntity.ok(watch);
    }

}