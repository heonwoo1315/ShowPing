package com.ssginc.showpingrefactoring.domain.watch.dto.object;

import java.time.LocalDateTime;

public record WatchHistoryCursor(LocalDateTime watchTime, Long streamNo) {
}
