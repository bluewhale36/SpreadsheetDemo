package com.example.spreadsheetdemo.herb.domain.entity;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.herb.dto.HerbLogDTO;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.function.Supplier;


public class HerbLog extends SheetsEntity {

    @Getter
    private LocalDateTime loggedDateTime;
    @Getter
    private String name;
    @Getter
    private Long beforeAmount;
    @Getter
    private Long afterAmount;

    private Supplier<Herb> herbSupplier;
    private Herb cachedHerb;

    public Herb getHerb() {
        return cachedHerb = cachedHerb == null ? herbSupplier.get() : cachedHerb;
    }

    private HerbLog(Integer rowNum, LocalDateTime loggedDateTime, String name, Long beforeAmount, Long afterAmount) {
        this(rowNum, loggedDateTime, name, beforeAmount, afterAmount, null);
    }

    private HerbLog(Integer rowNum, LocalDateTime loggedDateTime, String name, Long beforeAmount, Long afterAmount, Supplier<Herb> herbSupplier) {
        super(rowNum);
        this.loggedDateTime = loggedDateTime;
        this.name = name;
        this.beforeAmount = beforeAmount;
        this.afterAmount = afterAmount;
        this.herbSupplier = herbSupplier;
    }

    public static HerbLog of(
            Integer rowNum, LocalDateTime loggedDateTime, String name, Long beforeAmount, Long afterAmount
    ) {
        return HerbLog.of(rowNum, loggedDateTime, name, beforeAmount, afterAmount, null);
    }

    public static HerbLog of(
            Integer rowNum, LocalDateTime loggedDateTime, String name, Long beforeAmount, Long afterAmount, Supplier<Herb> herbSupplier
    ) {
        return new HerbLog(rowNum, loggedDateTime, name, beforeAmount, afterAmount, herbSupplier);
    }
}
