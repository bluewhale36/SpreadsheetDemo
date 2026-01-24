package com.example.spreadsheetdemo.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스프레드시트의 시트 정보와 데이터 범위 정의.<br/>
 * 스프레드시트의 시트 정보나 표 구조가 변경될 경우 그 수정 사항을 반영해야 함.
 */
@RequiredArgsConstructor
@Getter
public enum SheetsInfo {

    HERB("herb", "A", "D", 2, null),
    HERB_LOG("herb_log", "A", "D", 2, null);

    private final String sheetName;
    private final String startColumn;
    private final String endColumn;
    private final Integer startRowNum;
    private final Integer endRowNum;

    /**
     * 데이터 범위를 {@code '시트이름'!시작열:끝열} 형식으로 반환.
     * @return 데이터 범위 문자열
     */
    public String getDataRange() {
        return String.format("'%s'!%s:%s", sheetName, startColumn, endColumn);
    }

    public String getSpecificRowNum(int rowNum) {
        return String.format("'%s'!%s%d:%s%d", sheetName, startColumn, rowNum, endColumn, rowNum);
    }

    public String getSpecificRowRange(int startRowNum, int endRowNum) {
        return String.format("'%s'!%s%d:%s%d", sheetName, startColumn, startRowNum, endColumn, endRowNum);
    }
}
