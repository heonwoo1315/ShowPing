package com.ssginc.showpingrefactoring.domain.stream.controller;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.service.SubtitleService;
import com.ssginc.showpingrefactoring.domain.stream.service.VodService;
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
    public ResponseEntity<?> getVodListByCategory(@PathVariable Long categoryNo) {
        List<StreamResponseDto> vodList = vodService.getAllVodByCategory(categoryNo);
        Map<String, Object> result = new HashMap<>();

        result.put("vodList", vodList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 전체 Vod 목록을 반환해주는 컨트롤러 메서드
     * @param categoryNo 카테고리 번호
     * @return 전달할 응답객체 (json 형태로 전달)
     */
    @GetMapping("/list/category")
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
    public ResponseEntity<?> getSubtitle(@PathVariable String title) {
        Resource subtitleJson = subtitleService.getSubtitle(title);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subtitleJson);
    }

}
