package com.example.spreadsheetdemo.herb.dto;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 약재 정보 수정용 DTO
 */
@Getter
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

    public HerbUpdateDTO(
            Integer rowNum,
            String name,
            Long originalAmount, Long newAmount,
            LocalDate originalLastStoredDate, LocalDate newLastStoredDate,
            String originalMemo, String newMemo
    ) {
        if (rowNum == null) throw new IllegalArgumentException("Herb rowNum should not be null");
        if (rowNum <= SheetsInfo.HERB.getStartRowNum() && (SheetsInfo.HERB.getEndRowNum() == null || rowNum >= SheetsInfo.HERB.getEndRowNum())) throw new IllegalArgumentException("Herb rowNum should be between startRowNum and endRowNum of Herb Sheet.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Herb name should not be null or blank");

        this.rowNum = rowNum;
        this.name = name;

        this.originalAmount = originalAmount;
        this.newAmount = newAmount;

        this.originalLastStoredDate = originalLastStoredDate;
        this.newLastStoredDate = newLastStoredDate;

        this.originalMemo = originalMemo;
        this.newMemo = newMemo;
    }

    public boolean isChanged() {
        return isAmountChanged() || isLastStoredDateChanged() || isMemoChanged();
    }

    public boolean isAmountChanged() {
        return !Objects.equals(originalAmount, newAmount);
    }

    public boolean isLastStoredDateChanged() {
        return !Objects.equals(originalLastStoredDate, newLastStoredDate);
    }

    public boolean isMemoChanged() {
        return !Objects.equals(originalMemo, newMemo);
    }
}
