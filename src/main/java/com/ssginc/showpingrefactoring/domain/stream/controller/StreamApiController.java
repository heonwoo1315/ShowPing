package com.ssginc.showpingrefactoring.domain.stream.controller;

import com.ssginc.showpingrefactoring.domain.product.service.ProductService;
import com.ssginc.showpingrefactoring.domain.stream.dto.object.ProductItemDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.request.RegisterStreamRequestDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.request.StreamRequestDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.GetStreamRegisterInfoResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StartStreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class StreamApiController {

    private final StreamService streamService;

    private final ProductService productService;

    /**
     * 라이브 방송을 반환해주는 컨트롤러 메서드
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/onair")
    public ResponseEntity<?> getLive() {
        StreamResponseDto live = streamService.getLive();

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("live", live);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 라이브 중.예정 방송목록을 반환해주는 컨트롤러 메서드
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getBroadCast(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 4);
        Page<StreamResponseDto> pageInfo = streamService.getAllBroadCastByPage(pageable);

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("pageInfo", pageInfo);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 준비중인 방송목록을 반환해주는 컨트롤러 메서드
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/standby")
    public ResponseEntity<?> getStandByList(@RequestParam(defaultValue = "0", name = "pageNo") int pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 4);;
        Page<StreamResponseDto> pageInfo = streamService.getAllStandbyByPage(pageable);

        // Map으로 전달할 응답객체 저장
        Map<String, Object> result = new HashMap<>();
        result.put("pageInfo", pageInfo);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 방송 등록 화면에서 상품 선택을 위해 상품 목록을 반환해주는 메서드
     * @return 상품 목록이 포함된 응답 객체
     */
    @GetMapping("/product/list")
    public ResponseEntity<List<ProductItemDto>> getProductList() {
        List<ProductItemDto> productItemDtoList = productService.getProducts();

        return ResponseEntity.status(HttpStatus.OK).body(productItemDtoList);
    }

    /**
     * 방송 등록을 하는 메서드
     * @param request 방송 등록에 필요한 방송 정보
     * @return 생성 혹은 수정된 방송 데이터의 방송 번호가 포함된 응답 객체
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Long>> createStream(@AuthenticationPrincipal UserDetails userDetails, @RequestBody RegisterStreamRequestDto request) {
        String memberId = null;

        if (userDetails != null) {
            memberId = userDetails.getUsername();
        }

        Long streamNo = streamService.createStream(memberId, request);

        Map<String, Long> response = new HashMap<>();
        response.put("streamNo", streamNo);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 방송 시작을 하는 메서드
     * @param request streamNo가 담긴 요청 객체
     * @return 시작한 방송에 대한 정보
     */
    @PostMapping("/start")
    public ResponseEntity<StartStreamResponseDto> startStream(@RequestBody StreamRequestDto request) {
        StartStreamResponseDto startStreamResponseDto = streamService.startStream(request.getStreamNo());

        return ResponseEntity.status(HttpStatus.OK).body(startStreamResponseDto);
    }

    /**
     * 방송 종료를 하는 메서드
     * @param request streamNo가 담긴 요청 객체
     * @return 방송 종료 설정 적용 여부
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Boolean>> stopStream(@RequestBody StreamRequestDto request) {
        Boolean result = streamService.stopStream(request.getStreamNo());

        Map<String, Boolean> response = new HashMap<>();
        response.put("result", result);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 방송 페이지 이동시 필요한 정보를 가져오는 메서드
     * @param userDetails
     * @return 방송 정보가 담긴 응답 객체
     */
    @GetMapping("/live-info")
    public ResponseEntity<GetStreamRegisterInfoResponseDto> getStreamInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String memberId = null;
        if (userDetails != null) {
            memberId = userDetails.getUsername();
        }

        GetStreamRegisterInfoResponseDto response = streamService.getStreamRegisterInfo(memberId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
