package com.ssginc.showpingrefactoring.domain.stream.repository;

import com.ssginc.showpingrefactoring.domain.stream.dto.response.StreamResponseDto;
import com.ssginc.showpingrefactoring.domain.stream.entity.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VodRepository extends JpaRepository<Stream, Long> {

    /**
     * 전체 Vod 목록을 반환해주는 쿼리 메서드
     * @return vod 목록
     */
    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription ,s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ENDED'
    """)
    List<StreamResponseDto> findAllVod();

    /**
     * VOD 목록과 페이지 정보를 반환해주는 쿼리 메서드
     * @param pageable 페이징 정보 객체
     * @return 페이징 정보가 포함된 VOD 목록
     */
    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription, s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ENDED'
        ORDER BY s.streamNo DESC
    """)
    Page<StreamResponseDto> findAllVodByPage(Pageable pageable);

    /**
     * 특정 카테고리의 Vod 목록을 반환해주는 쿼리 메서드
     * @param categoryNo 카테고리 번호
     * @return vod 목록
     */
    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription, s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ENDED'
        AND c.categoryNo = :categoryNo ORDER BY s.streamNo DESC
    """)
    List<StreamResponseDto> findAllVodByCategory(Long categoryNo);

    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription ,s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ENDED'
        AND c.categoryNo = :categoryNo ORDER BY s.streamNo DESC
    """)
    Page<StreamResponseDto> findAllVodByCategoryAndPage(Long categoryNo, Pageable pageable);

    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription ,s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Watch w ON w.stream.streamNo = s.streamNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamStatus = 'ENDED'
        GROUP BY w.stream.streamNo ORDER BY count(w.stream.streamNo) DESC
    """)
    Page<StreamResponseDto> findAllVodByWatch(Pageable pageable);

    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription ,s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Watch w ON s.streamNo = w.stream.streamNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo
        WHERE s.streamStatus = 'ENDED' AND c.categoryNo = :categoryNo
        GROUP BY w.stream.streamNo ORDER BY count(w.stream.streamNo) DESC
    """)
    Page<StreamResponseDto> findAllVodByCategoryAndWatch(Long categoryNo, Pageable pageable);

    /**
     * 특정 영상번호의 VOD 정보를 반환해주는 쿼리 메서드
     * @param streamNo 영상 번호
     * @return VOD 정보
     */
    @Query("""
        SELECT new com.ssginc.showpinglive.dto.response.StreamResponseDto
        (s.streamNo, s.streamTitle, s.streamDescription, s.streamStatus, c.categoryNo, c.categoryName, p.productName,
        p.productPrice, p.productSale, p.productImg, s.streamStartTime, s.streamEndTime)
        FROM Stream s JOIN Product p ON s.product.productNo = p.productNo
        JOIN Category c ON p.category.categoryNo = c.categoryNo WHERE s.streamNo = :streamNo
    """)
    StreamResponseDto findVodByNo(Long streamNo);

}
