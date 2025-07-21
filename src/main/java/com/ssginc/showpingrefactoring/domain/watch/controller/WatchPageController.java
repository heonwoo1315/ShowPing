package com.ssginc.showpingrefactoring.domain.watch.controller;

import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.member.service.MemberService;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.GetLiveProductInfoResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.service.LiveService;
import com.ssginc.showpingrefactoring.domain.stream.service.VodService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author dckat
 * 영상 시청과 관련한 페이지 요청-응답을 수행하는 컨트롤러 클래스
 * <p>
 */
@Controller
@RequestMapping("watch")
@RequiredArgsConstructor
public class WatchPageController {

    private final LiveService streamService;

    private final VodService vodService;

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

        GetLiveProductInfoResponseDto streamProductInfo = streamService.getStreamProductInfo(streamNo);

        // VOD 객체 정보 불러오기
        StreamResponseDto vodDto = vodService.getVodByNo(streamNo);
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

}
