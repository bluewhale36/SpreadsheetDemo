package com.example.spreadsheetdemo.herb.domain.entity;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.herb.dto.HerbDTO;
import com.example.spreadsheetdemo.herb.dto.HerbRegisterDTO;
import com.example.spreadsheetdemo.herb.dto.HerbUpdateDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString(callSuper = true)
public class Herb extends SheetsEntity {

    private String name;
    private Long amount;
    private LocalDate lastStoredDate;
    private String memo;

    @Builder
    private Herb(Integer rowNum, String name, Long amount, LocalDate lastStoredDate, String memo) {
        super(rowNum);
        this.name = name;
        this.amount = amount;
        this.lastStoredDate = lastStoredDate;
        this.memo = memo;
    }

    private Herb(Integer rowNum, String name, Long amount) {
        this(rowNum, name, amount, null, null);
    }

    private Herb(Integer rowNum, String name, Long amount, LocalDate lastStoredDate) {
        this(rowNum, name, amount, lastStoredDate, null);
    }

    public static Herb create(HerbRegisterDTO dto) {
        return new Herb(
                null,
                dto.getName(),
                dto.getAmount(),
                dto.getLastStoredDate(),
                dto.getMemo()
        );
    }

    public static Herb of(Integer rowNum, String name, Long amount) {
        return new Herb(rowNum, name, amount);
    }

    public static Herb of(Integer rowNum, String name, Long amount, LocalDate lastStoredDate) {
        return new Herb(rowNum, name, amount, lastStoredDate);
    }

    public static Herb of(Integer rowNum, String name, Long amount, LocalDate lastStoredDate, String memo) {
        return new Herb(rowNum, name, amount, lastStoredDate, memo);
    }

}
