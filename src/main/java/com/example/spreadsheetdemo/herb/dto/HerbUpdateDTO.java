package com.example.spreadsheetdemo.herb.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * 약재 정보 수정용 DTO
 */
@Getter
@Builder
@RequiredArgsConstructor // Controller 의 @RequestBody 바인딩 목적
@ToString
@EqualsAndHashCode
public class HerbUpdateDTO {

    private final Integer rowNum;
    private final String name;

    private final Long originalAmount;
    private final Long newAmount;

    private final LocalDate originalLastStoredDate;
    private final LocalDate newLastStoredDate;

    private final String originalMemo;
    private final String newMemo;

    public boolean isChanged() {
        return isAmountChanged() || isLastStoredDateChanged() || isMemoChanged();
    }

    public boolean isAmountChanged() {
        return !originalAmount.equals(newAmount);
    }

    public boolean isLastStoredDateChanged() {
        return !originalLastStoredDate.equals(newLastStoredDate);
    }

    public boolean isMemoChanged() {
        return !originalMemo.equals(newMemo);
    }
}
