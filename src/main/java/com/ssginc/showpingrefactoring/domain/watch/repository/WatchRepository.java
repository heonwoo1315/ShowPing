package com.ssginc.showpingrefactoring.domain.watch.repository;

import com.ssginc.showpingrefactoring.domain.watch.dto.response.WatchResponseDto;
import com.ssginc.showpingrefactoring.domain.watch.entity.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

}
