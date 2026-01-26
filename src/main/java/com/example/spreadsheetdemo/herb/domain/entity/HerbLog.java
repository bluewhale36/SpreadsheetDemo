package com.example.spreadsheetdemo.herb.domain.entity;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@ToString(callSuper = true)
public class HerbLog extends SheetsEntity {

    @Getter
    private LocalDateTime loggedDateTime;
    @Getter
    private String name;
    @Getter
    private Long beforeAmount;
    @Getter
    private Long afterAmount;

    @ToString.Exclude
    private Supplier<Herb> herbSupplier;
    @ToString.Exclude
    private Herb cachedHerb;

    public Herb getHerb() {
        if (cachedHerb == null) {
            if (herbSupplier == null) {
                throw new IllegalStateException("Tried to get Herb entity while herbSupplier has not been injected.");
            }
            cachedHerb = herbSupplier.get();
        }
        return cachedHerb;
    }

    @Builder
    public HerbLog(Integer rowNum, LocalDateTime loggedDateTime, String name, Long beforeAmount, Long afterAmount, Supplier<Herb> herbSupplier) {
        super(rowNum);

        this.loggedDateTime = (loggedDateTime != null) ? loggedDateTime : LocalDateTime.now();

        if (name == null || name.isBlank()) throw new IllegalArgumentException("Herb name should not be null while creating HerbLog entity.");
        this.name = name;

        this.beforeAmount = (beforeAmount == null) ? 0L : beforeAmount;
        this.afterAmount = (afterAmount == null) ? 0L : afterAmount;

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
