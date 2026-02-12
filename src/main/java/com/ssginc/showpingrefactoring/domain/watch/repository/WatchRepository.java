package com.ssginc.showpingrefactoring.domain.watch.repository;

import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.entity.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dckat
 * 영상 시청과 관련한 쿼리메소드를 수행하는 클래스
 * <p>
 */
@Repository
public interface WatchRepository extends JpaRepository<Watch, Long> {

    /**
     * 로그인한 사용자의 시청내역 리스트를 반환하는 메서드
     * @param memberNo 로그인한 사용자 번호
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Query("""
        SELECT new com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto
        (w.stream.streamNo, s.streamTitle, p.productImg, p.productName, p.productPrice, MAX(w.watchTime))
        FROM Watch w JOIN Stream s ON w.stream.streamNo = s.streamNo
        JOIN Product p ON s.product.productNo = p.productNo WHERE w.member.memberNo = :memberNo
        GROUP BY w.stream.streamNo, s.streamTitle, p.productImg, p.productName, p.productPrice
    """)
    List<WatchResponseDto> getWatchListByMemberNo(Long memberNo);


    /**
     * 로그인한 사용자의 시청내역 리스트를 페이지네이션하여 반환하는 메서드 (성능개선 없음)
     * @param memberNo 로그인한 사용자 번호
     * @param fromDate 필터링 시작일
     * @param toDate   필터링 종료일
     * @param pageable 페이지네이션 객체
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Query("""
        SELECT new com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto
        (w.stream.streamNo, s.streamTitle, p.productImg, p.productName, p.productPrice, MAX(w.watchTime))
        FROM Watch w JOIN Stream s ON w.stream.streamNo = s.streamNo
        JOIN Product p ON s.product.productNo = p.productNo
        WHERE w.member.memberNo = :memberNo AND (:fromDate IS NULL OR w.watchTime >= :fromDate) AND w.watchTime <= :toDate
        GROUP BY w.stream.streamNo, s.streamTitle, p.productImg, p.productName, p.productPrice
        ORDER BY MAX(w.watchTime) DESC
    """)
    Page<WatchRowProjection> getWatchHistoryPageByMemberAndDate(@Param("memberNo") Long memberNo,
                                                                @Param("fromDate") LocalDateTime fromDate,
                                                                @Param("toDate") LocalDateTime toDate,
                                                                Pageable pageable);

    /**
     * 로그인한 사용자의 시청내역을 페이지네이션하여 보여주는 쿼리 메서드 (커서기반)
     * @param memberNo       로그인한 사용자 번호
     * @param fromDate       필터링 시작일
     * @param toDate         필터링 종료일
     * @param cursorTime     페이징커서 기반 시청시각
     * @param cursorStreamNo 페이징커서 기반 영상번호
     * @param limitPlusOne   페이징커서 기반 데이터 갯수 (다음 데이터를 보여줘야 하므로 하나 더하여 수행
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Query(value = """ 
        SELECT s.stream_no AS streamNo, 
               s.stream_title AS streamTitle, 
               p.product_img AS productImg, 
               p.product_name AS productName, 
               p.product_price AS productPrice, 
               x.latest_time AS watchTime 
        FROM ( 
            SELECT w.stream_no AS stream_no, 
                   MAX(w.watch_time) AS latest_time 
            FROM watch w WHERE w.member_no = :memberNo 
                           AND (:fromDate IS NULL OR w.watch_time >= :fromDate) 
                           AND (:toDate IS NULL OR w.watch_time < :toDate) 
                         GROUP BY w.stream_no ) x 
            JOIN stream s ON s.stream_no = x.stream_no 
            JOIN product p ON p.product_no = s.product_no 
        /* 커서 조건 */ 
        WHERE ( 
            (:cursorTime IS NULL AND :cursorStreamNo IS NULL) 
                OR x.latest_time < :cursorTime 
                OR (x.latest_time = :cursorTime 
                        AND s.stream_no < :cursorStreamNo) ) 
        ORDER BY x.latest_time DESC, s.stream_no DESC 
        LIMIT :limitPlusOne 
    """,
            nativeQuery = true )
    List<WatchRowProjection> getWatchHistoryPageScrollV1(
            @Param("memberNo")        Long memberNo,
            @Param("fromDate")        LocalDateTime fromDate,
            @Param("toDate")          LocalDateTime toDate,
            @Param("cursorTime")      LocalDateTime cursorTime,
            @Param("cursorStreamNo")  Long cursorStreamNo,
            @Param("limitPlusOne")    int limitPlusOne
    );


    /**
     * 로그인한 사용자의 시청내역을 페이지네이션하여 보여주는 쿼리 메서드 (커서기반 + 윈도우 함수 활용 GROUP BY 및 MAX 개선)
     * @param memberNo       로그인한 사용자 번호
     * @param fromDate       필터링 시작일
     * @param toDate         필터링 종료일
     * @param cursorTime     페이징커서 기반 시청시각
     * @param cursorStreamNo 페이징커서 기반 영상번호
     * @param limitPlusOne   페이징커서 기반 데이터 갯수 (다음 데이터를 보여줘야 하므로 하나 더하여 수행
     * @return 로그인한 사용자의 시청내역 응답 객체
     */
    @Query(value = """
        SELECT
          s.stream_no      AS streamNo,
          s.stream_title   AS streamTitle,
          p.product_img    AS productImg,
          p.product_name   AS productName,
          p.product_price  AS productPrice,
          w.watch_time     AS watchTime
        FROM (
            SELECT w.stream_no, w.watch_time,
                   ROW_NUMBER() OVER (
                     PARTITION BY w.stream_no
                     ORDER BY w.watch_time DESC
                   ) AS rn
            FROM watch w
            WHERE w.member_no = ?1
              AND (?2 IS NULL OR w.watch_time >= ?2)
              AND (?3 IS NULL OR w.watch_time <  ?3)
        ) w
        JOIN stream s  ON s.stream_no = w.stream_no
        JOIN product p ON p.product_no = s.product_no
        WHERE w.rn = 1
          AND (
            (?4 IS NULL AND ?5 IS NULL)
            OR w.watch_time < ?4
            OR (w.watch_time = ?4 AND w.stream_no < ?5)
          )
        ORDER BY w.watch_time DESC, w.stream_no DESC
        LIMIT ?6
        """,
            nativeQuery = true)
    List<WatchRowProjection> getWatchHistoryPageScrollV2(
            Long memberNo, LocalDateTime fromDate, LocalDateTime toDate,
            LocalDateTime cursorTime, Long cursorNo, int limit
    );

}
