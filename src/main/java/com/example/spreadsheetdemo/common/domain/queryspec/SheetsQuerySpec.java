package com.example.spreadsheetdemo.common.domain.queryspec;

import com.example.spreadsheetdemo.common.SheetsInfo;
import lombok.Getter;

@Getter
public abstract class SheetsQuerySpec {

    protected final SheetsInfo sheetsInfo;

    protected final String startColumn;
    protected final String endColumn;

    protected final Integer startRowNum;
    protected final Integer endRowNum;

    protected SheetsQuerySpec(
            SheetsInfo sheetsInfo, String startColumn, String endColumn, Integer startRowNum, Integer endRowNum
    ) {
        if (sheetsInfo == null) throw new IllegalArgumentException("sheetsInfo is null");
        if (startColumn == null && endColumn != null) throw new IllegalArgumentException("endColumn should be null when startColumn is null.");
        if (startRowNum == null && endRowNum != null) throw new IllegalArgumentException("endRowNum should be null when startRowNum is null.");

        this.sheetsInfo = sheetsInfo;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.startRowNum = startRowNum;
        this.endRowNum = endRowNum;
    }

    protected SheetsQuerySpec(SheetsInfo sheetsInfo, Integer startRowNum, Integer endRowNum) {
        this(
                sheetsInfo,
                sheetsInfo.getStartColumn(), sheetsInfo.getEndColumn(),
                startRowNum, endRowNum
        );
    }

    protected SheetsQuerySpec(SheetsInfo sheetsInfo, String startColumn, String endColumn) {
        this(
                sheetsInfo,
                startColumn, endColumn,
                null, null
        );
    }

    protected SheetsQuerySpec(SheetsInfo sheetsInfo) {
        this(
                sheetsInfo,
                sheetsInfo.getStartColumn(), sheetsInfo.getEndColumn(),
                sheetsInfo.getStartRowNum(), sheetsInfo.getEndRowNum()
        );
    }

    /**
     * 지정된 쿼리 범위를 A1 스타일 표기법으로 반환.
     *
     * @return A1 스타일 표기법으로 변환된 쿼리 범위 문자열.
     * @see <a href=https://developers.google.com/workspace/sheets/api/guides/concepts?hl=ko#cell>A1 스타일 표기법</a>
     */
    public String getQueryRangeAsA1Notation() {
        StringBuilder query = new StringBuilder();

        // 시트 이름 정보
        query.append(sheetsInfo.getSheetName());
        query.append("!");

        // 시작 열 및 행 번호
        query.append(sheetsInfo.getStartColumn());
        if (startRowNum != null && startRowNum > 0) {
            query.append(startRowNum);
        }
        query.append(":");

        // 마지막 열 및 행 번호
        query.append(sheetsInfo.getEndColumn());
        if (endRowNum != null && endRowNum > 0) {
            query.append(endRowNum);
        }

        return query.toString();
    }
}
