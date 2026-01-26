package com.example.spreadsheetdemo.herb.dto;

import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 약재 로그 기록용 DTO
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class HerbLogDTO {

    private final LocalDateTime loggedDateTime;
    private final String name;
    private final Long beforeAmount;
    private final Long afterAmount;

    public boolean isAmountIncreased() {
        return afterAmount > beforeAmount;
    }

    public static HerbLogDTO from(HerbLog herbLog) {
        if (herbLog == null) return null;

        return HerbLogDTO.builder()
                .loggedDateTime(herbLog.getLoggedDateTime())
                .name(herbLog.getName())
                .beforeAmount(herbLog.getBeforeAmount())
                .afterAmount(herbLog.getAfterAmount())
                .build();
    }
}
