package com.ssginc.showpingrefactoring.domain.stream.controller;

import com.ssginc.showpingrefactoring.domain.chat.dto.ChatRoomResponseDto;
import com.ssginc.showpingrefactoring.domain.chat.service.ChatRoomService;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.GetLiveProductInfoResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.service.LiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("stream")
@RequiredArgsConstructor
public class StreamPageController {

    private final LiveService liveService;

    private final ChatRoomService chatRoomService;

    /**
     * 방송 등록 페이지 이동 메서드
     * @return 방송 등록 View 반환
     */
    @GetMapping("/stream")
    public String streamPage() {
        return "stream/stream";
    }

    /**
     * 라이브 메인 페이지 요청 컨틀롤러 메서드
     * @return 라이브 메인 페이지 (타임리프)
     */
    @GetMapping("/list")
    public String streamList() {
        return "stream/list";
    }

    /**
     * 방송 시청 페이지 이동 메서드
     * @param streamNo 시청하려는 방송 번호
     * @param model 방송 시청 View에 필요한 속성을 추가해주기 위한 Model 객체
     * @return 방송 시청 View 반환
     */
    @GetMapping("watch/{streamNo}")
    public String watch(@PathVariable Long streamNo, Model model) {

        GetLiveProductInfoResponseDto liveProductInfo = liveService.getStreamProductInfo(streamNo);
        ChatRoomResponseDto chatRoom = chatRoomService.findChatRoomByStreamNo(streamNo);

        // 가격 문자열에서 숫자와 소수점만 남김 (쉼표, 원 등의 문자는 제거)
        String rawPrice = liveProductInfo.getProductPrice().replaceAll("[^\\d.]", "");
        String rawSale = liveProductInfo.getProductSalePrice().replaceAll("[^\\d.]", "");

        int price = Integer.parseInt(rawPrice);
        int sale = Integer.parseInt(rawSale);

        // 할인 금액: (상품 가격 - 할인가격)
        int discountAmount = price - sale;
        // 할인율: ((할인 금액 / 상품 가격) * 100)
        int discountRate = (price > 0) ? (discountAmount * 100) / price : 0;

        model.addAttribute("chatRoomInfo", chatRoom);
        model.addAttribute("productInfo", liveProductInfo);
        model.addAttribute("discountRate", discountRate);

        return "stream/watch";
    }

}
