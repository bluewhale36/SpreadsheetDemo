package com.example.spreadsheetdemo.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스프레드시트의 시트 정보와 데이터 범위 정의.
 */
@RequiredArgsConstructor
@Getter
public enum SheetsInfo {

    HERB("herb", "A", "D"),
    HERB_LOG("herb_log", "A", "D");

    private final String sheetName;
    private final String startColumn;
    private final String endColumn;

    /**
     * 데이터 범위를 {@code 시트이름!시작열:끝열} 형식으로 반환.
     * @return 데이터 범위 문자열
     */
    public String getDataRange() {
        return String.format("%s!%s:%s", sheetName, startColumn, endColumn);
    }
}
