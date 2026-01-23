package com.example.spreadsheetdemo.herb.domain.entity;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.herb.dto.HerbLogDTO;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HerbLog extends SheetsEntity {

    private LocalDateTime loggedDateTime;
    private Herb herb;
    private Long beforeAmount;
    private Long afterAmount;

    private HerbLog(Integer rowNum, LocalDateTime loggedDateTime, Herb herb, Long beforeAmount, Long afterAmount) {
        super(rowNum);
        this.loggedDateTime = loggedDateTime;
        this.herb = herb;
        this.beforeAmount = beforeAmount;
        this.afterAmount = afterAmount;
    }

    public static HerbLog create(HerbLogDTO herbLogDTO, Herb herb) {
        return new HerbLog(
                herb.getRowNum(),
                herbLogDTO.getLoggedDatetime(),
                herb,
                herbLogDTO.getBeforeAmount(),
                herbLogDTO.getAfterAmount()
        );
    }
}
