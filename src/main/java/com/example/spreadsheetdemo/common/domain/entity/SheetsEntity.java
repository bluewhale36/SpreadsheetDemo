package com.example.spreadsheetdemo.common.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class SheetsEntity {

    protected Integer rowNum;

    protected SheetsEntity(Integer rowNum) {
        this.rowNum = rowNum;
    }
}
