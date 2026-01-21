package com.example.spreadsheetdemo.herb.dto;

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

    private final LocalDateTime loggedDatetime;
    private final String name;
    private final Long beforeAmount;
    private final Long afterAmount;

    public boolean isAmountIncreased() {
        return afterAmount > beforeAmount;
    }
}
