package com.ssginc.showpingrefactoring.domain.stream.repository;

import com.ssginc.showpingrefactoring.domain.stream.dto.object.GetStreamRegisterInfoDto;
import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.entity.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StreamRepository extends JpaRepository<Stream, Long> {

    /**
     * 진행중인 라이브 방송을 반환해주는 쿼리 메서드
     * @return 라이브 방송 정보 리스트
     */
    @Query("""
        SELECT new com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription, s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ONAIR'
        ORDER BY s.streamNo DESC
    """)
    List<StreamResponseDto> findLive();

    /**
     * 진행중인 라이브 방송을 반환해주는 쿼리 메서드
     * @return 라이브 방송 정보 리스트
     */
    @Query("""
        SELECT new com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription, s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ONAIR' OR s.streamStatus = 'STANDBY'
        ORDER BY s.streamNo DESC, s.streamStatus ASC
    """)
    Page<StreamResponseDto> findAllBroadCastByPage(Pageable pageable);

    /**
     * 준비중인 라이브 목록과 페이지 정보를 반환해주는 쿼리 메서드
     * @param pageable 페이징 정보 객체
     * @return 페이징 정보가 포함된 준비중인 라이브 목록
     */
    @Query("""
        SELECT new com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription, s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'STANDBY'
        ORDER BY s.streamNo DESC
    """)
    Page<StreamResponseDto> findAllStandbyByPage(Pageable pageable);

    /**
     * 로그인한 회원 정보와 STANDBY 상태인 방송 조회 메서드
     * @param memberId 회원 아이디
     * @return stream 정보
     */
    @Query("""
        SELECT new com.ssginc.showpingrefactoring.domain.stream.dto.object.GetStreamRegisterInfoDto
        (s.streamNo, s.streamTitle, s.streamDescription,
        p.productNo, p.productName, p.productPrice, p.productSale, p.productImg)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        WHERE s.member.memberId = :memberId AND s.streamStatus = "STANDBY"
    """)
    GetStreamRegisterInfoDto findStreamByMemberIdAndStreamStatus(String memberId);

}
