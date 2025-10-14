package com.ssginc.showpingrefactoring.domain.watch.service.implement;

import com.ssginc.showpingrefactoring.common.dto.SliceResponseDto;
import com.ssginc.showpingrefactoring.common.exception.CustomException;
import com.ssginc.showpingrefactoring.common.exception.ErrorCode;
import com.ssginc.showpingrefactoring.domain.member.entity.Member;
import com.ssginc.showpingrefactoring.domain.stream.entity.Stream;
import com.ssginc.showpingrefactoring.domain.watch.dto.object.WatchHistoryCursor;
import com.ssginc.showpingrefactoring.domain.watch.dto.request.WatchRequestDto;
import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.entity.Watch;
import com.ssginc.showpingrefactoring.domain.watch.repository.WatchRepository;
import com.ssginc.showpingrefactoring.domain.watch.repository.WatchRowProjection;
import com.ssginc.showpingrefactoring.domain.watch.service.WatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author dckat
 * 영상 시청과 관련한 로직을 처리하는 서비스 layer 클래스
 * <p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchServiceImpl implements WatchService {

    private final WatchRepository watchRepository;

    /**
     * 로그인한 사용자의 시청내역 리스트를 반환하는 컨트롤러 메소드
     * @param memberNo 로그인한 사용자 번호
     * @return 로그인한 사용자의 시청내역 리스트
     */
    @Override
    public List<WatchResponseDto> getWatchHistoryByMemberNo(Long memberNo) {
        List<WatchResponseDto> watchList = watchRepository.getWatchListByMemberNo(memberNo);

        if (watchList.isEmpty()) {
            throw new CustomException(ErrorCode.WATCH_LIST_EMPTY);
        }
        return watchList;
    }

    /**
     * 로그인한 사용자의 시청내역 리스트를 페이지네이션하여 반환하는 메서드 (성능개선 없음)
     * @param memberNo 로그인한 사용자 번호
     * @param fromDate 필터링 시작일
     * @param toDate   필터링 종료일
     * @param pageable 페이지네이션 객체
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Override
    public Page<WatchResponseDto> getWatchHistoryPage(Long memberNo,
                                                      LocalDateTime fromDate,
                                                      LocalDateTime toDate,
                                                      Pageable pageable) {

        Page<WatchRowProjection> page = watchRepository.getWatchHistoryPageByMemberAndDate(memberNo,
                fromDate,
                toDate,
                pageable);

        return page.map(p -> new WatchResponseDto(
                p.getStreamNo(), p.getStreamTitle(),
                p.getProductImg(), p.getProductName(),
                p.getProductPrice(), p.getWatchTime()
        ));
    }

    /**
     * 로그인한 사용자의 시청내역을 페이지네이션하여 보여주는 쿼리 메서드 (커서기반)
     * @param memberNo       로그인한 사용자 번호
     * @param fromDate       필터링 시작일
     * @param toDate         필터링 종료일
     * @param cursor         페이징 커서 객체 (시청시간. 영상번호)
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Override
    public SliceResponseDto<WatchResponseDto, WatchHistoryCursor> getWatchHistoryPageScroll(Long memberNo,
                                                                                            LocalDateTime fromDate,
                                                                                            LocalDateTime toDate,
                                                                                            WatchHistoryCursor cursor,
                                                                                            int size) {
        /* 윈도우 함수 미적용 커서기반 페이지네이션
        List<WatchRowProjection> rows = watchRepository.getWatchHistoryPageScrollV1(
                memberNo,
                fromDate == null ? null : fromDate.toLocalDateTime(),
                toDate == null ? null : toDate.toLocalDateTime(),
                (cursor == null ? null : cursor.watchTime()),
                (cursor == null ? null : cursor.streamNo()),
                size+1
        );
         */


        // 윈도우 함수 적용 커서기반 페이지네이션
        List<WatchRowProjection> rows = watchRepository.getWatchHistoryPageScrollV2(
                memberNo,
                fromDate,
                toDate,
                (cursor == null ? null : cursor.watchTime()),
                (cursor == null ? null : cursor.streamNo()),
                size+1
        );

        boolean hasMore = rows.size() > size;
        if (hasMore) {
            rows = rows.subList(0, size);
        }

        WatchHistoryCursor nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            var last = rows.get(rows.size() - 1);
            nextCursor = new WatchHistoryCursor(last.getWatchTime(), last.getStreamNo());
        }

        List<WatchResponseDto> content = rows.stream()
                .map(r -> new WatchResponseDto(
                        r.getStreamNo(),
                        r.getStreamTitle(),
                        r.getProductImg(),
                        r.getProductName(),
                        r.getProductPrice(),
                        r.getWatchTime()
                ))
                .toList();

        return SliceResponseDto.of(content, hasMore, nextCursor);
    }

    /**
     * 시청 내역 등록 서비스 layer 메소드
     * @param watchRequestDto 시청내역 등록을 위한 요청 DTO
     * @param memberNo 시청한 회원 번호
     * @return 추가된 시청내역 객체
     */
    @Override
    public Watch insertWatchHistory(WatchRequestDto watchRequestDto, Long memberNo) {
        // 영상 엔티티 객체 생성 (빌더 패턴)
        Stream stream = Stream.builder()
                .streamNo(watchRequestDto.getStreamNo())
                .build();

        // 사용자 엔티티 객체 생성
        // 로그인하지 않은 사용자도 insert 하기 위해 우선적으로 null 할당
        Member member = null;

        // 로그인한 사용자가 존재한 경우
        if (memberNo != null) {
            member = Member.builder()
                    .memberNo(memberNo)
                    .build();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(watchRequestDto.getWatchTime().substring(0, 19), formatter);

        // DB 저장을 위한 엔티티 객체 생성 (빌더 패턴)
        Watch watch = Watch.builder()
                .stream(stream)
                .member(member)
                .watchTime(dateTime)
                .build();

        return watchRepository.save(watch);
    }

}
