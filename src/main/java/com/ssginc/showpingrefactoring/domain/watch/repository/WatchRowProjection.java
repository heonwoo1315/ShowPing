package com.ssginc.showpingrefactoring.domain.watch.repository;

import java.time.LocalDateTime;

// 시청내역 네이티브 쿼리 매핑 클래스
public interface WatchRowProjection {
    Long   getStreamNo();
    String getStreamTitle();
    String getProductImg();
    String getProductName();
    Long   getProductPrice();
    LocalDateTime getWatchTime();
}

