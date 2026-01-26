package com.example.spreadsheetdemo.herb.domain.entity;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.herb.dto.HerbDTO;
import com.example.spreadsheetdemo.herb.dto.HerbRegisterDTO;
import com.example.spreadsheetdemo.herb.dto.HerbUpdateDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;


@Getter
@ToString(callSuper = true)
public class Herb extends SheetsEntity {

    private String name;
    private Long amount;
    private LocalDate lastStoredDate;
    private String memo;

    @Builder
    public Herb(
            Integer rowNum, String name, Long amount, LocalDate lastStoredDate, String memo
    ) {
        super(rowNum);
        this.name = name;
        this.amount = amount;
        this.lastStoredDate = lastStoredDate;
        this.memo = memo;
    }

    public static Herb create(HerbRegisterDTO dto) {
        if (dto == null) throw new IllegalArgumentException("HerbRegisterDTO should not be null while creating Herb entity.");

        return new Herb(
                null,
                dto.getName(),
                dto.getAmount(),
                dto.getLastStoredDate(),
                dto.getMemo()
        );
    }

    public static Herb of(Integer rowNum, String name, Long amount, LocalDate lastStoredDate, String memo) {
        return new Herb(rowNum, name, amount, lastStoredDate, memo);
    }

}
