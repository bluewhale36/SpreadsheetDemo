package com.example.spreadsheetdemo.common.domain.queryspec;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import lombok.Getter;

import java.util.function.Predicate;

@Getter
public class SheetsQuerySpec<ENTITY extends SheetsEntity> {

    protected final SheetsInfo sheetsInfo;

    protected final String startColumn;
    protected final String endColumn;

    protected final Integer startRowNum;
    protected final Integer endRowNum;

    protected final Predicate<ENTITY> queryCondition;

    protected SheetsQuerySpec(
            SheetsInfo sheetsInfo,
            String startColumn, String endColumn,
            Integer startRowNum, Integer endRowNum,
            Predicate<ENTITY> queryCondition
    ) {
        if (sheetsInfo == null) throw new IllegalArgumentException("sheetsInfo cannot be null");
        if (startColumn == null && endColumn != null) throw new IllegalArgumentException("endColumn should be null when startColumn is null.");
        if (startRowNum == null && endRowNum != null) throw new IllegalArgumentException("endRowNum should be null when startRowNum is null.");

        this.sheetsInfo = sheetsInfo;
        this.startColumn = startColumn;
        this.endColumn = endColumn == null ? sheetsInfo.getEndColumn() : endColumn;
        this.startRowNum = startRowNum;
        this.endRowNum = endRowNum == null ? sheetsInfo.getEndRowNum() : endRowNum;
        this.queryCondition = queryCondition;
    }

    protected SheetsQuerySpec(SheetsInfo sheetsInfo, Predicate<ENTITY> queryCondition) {
        this(
                sheetsInfo,
                sheetsInfo.getStartColumn(), sheetsInfo.getEndColumn(),
                sheetsInfo.getStartRowNum(), sheetsInfo.getEndRowNum(), null
        );
    }

    /**
     * 시트의 모든 데이터를 대상 범위로 하는 {@link SheetsQuerySpec} 객체를 반환함.
     *
     * @param sheetsInfo 범위를 선택할 대상 시트.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return 전달된 시트의 모든 데이터를 대상 범위로 하는 {@link SheetsQuerySpec} 객체.
     * @param <ENTITY> 전달된 시트의 데이터를 저장하는 {@link SheetsEntity} 를 상속하는 Entity Class.
     */
    public static <ENTITY extends SheetsEntity> SheetsQuerySpec<ENTITY> ofAllDataRange(SheetsInfo sheetsInfo, Predicate<ENTITY> queryCondition) {
        return new SheetsQuerySpec<>(sheetsInfo, queryCondition);
    }

    /**
     * 시트의 특정 열 범위에 대한 모든 데이터를 대상 범위로 하는 {@link SheetsQuerySpec} 객체를 반환함.
     *
     * @param sheetsInfo 범위를 선택할 대상 시트.
     * @param startColumn 시작 열 번호 문자열.
     * @param endColumn 종료 열 번호 문자열. {@code null} 값 전달 시, {@link SheetsInfo#endColumn} 까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return 전달된 시트의 시작-종료 열 번호의 모든 데이터를 대상 범위로 하는 {@link SheetsQuerySpec} 객체.
     * @param <ENTITY> 전달된 시트의 데이터를 저장하는 {@link SheetsEntity} 를 상속하는 Entity Class.
     */
    public static <ENTITY extends SheetsEntity> SheetsQuerySpec<ENTITY> ofAllRowDataWithSpecificColumnRange(
            SheetsInfo sheetsInfo, String startColumn, String endColumn, Predicate<ENTITY> queryCondition
    ) {
        return new SheetsQuerySpec<>(
                sheetsInfo,
                startColumn, endColumn,
                sheetsInfo.getStartRowNum(), sheetsInfo.getEndRowNum(),
                queryCondition
        );
    }

    /**
     * 시트의 특정 행 범위에 대한 모든 데이터를 대상 범위로 하는 {@link SheetsQuerySpec} 객체를 반환함.
     *
     * @param sheetsInfo 범위를 선택할 대상 시트.
     * @param startRowNum 시작 행 번호.
     * @param endRowNum 종료 행 번호. {@code null} 값 전달 시, {@link SheetsInfo#endRowNum} 까지의 범위로 지정되거나, 시작 행 번호 이후의 유효한 모든 행 번호까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return 전달된 시트의 시작-종료 행 번호의 모든 데이터를 대상 범위로 하는 {@link SheetsQuerySpec} 객체.
     * @param <ENTITY> 전달된 시트의 데이터를 저장하는 {@link SheetsEntity} 를 상속하는 Entity Class.
     */
    public static <ENTITY extends SheetsEntity> SheetsQuerySpec<ENTITY> ofAllColumnDataWithSpecificRowRange(
            SheetsInfo sheetsInfo, int startRowNum, Integer endRowNum, Predicate<ENTITY> queryCondition
    ) {
        return new SheetsQuerySpec<>(
                sheetsInfo,
                sheetsInfo.getStartColumn(), sheetsInfo.getEndColumn(),
                startRowNum, endRowNum,
                queryCondition
        );
    }

    /**
     * {@link SheetsQuerySpec#queryCondition} 에 맞는 {@link SheetsEntity} 상속의 Entity 객체를 필터링 한 결과를 반환함.
     *
     * @param entity {@link SheetsQuerySpec} 의 Type Parameter 인 Entity 객체.
     * @return {@link SheetsQuerySpec#queryCondition} 이 {@code null} 값일 경우 {@code true} 를 반환하며, 그렇지 않을 경우 {@link Predicate#test} 의 반환값.
     */
    public final boolean matches(ENTITY entity) {
        return queryCondition == null || queryCondition.test(entity);
    }

    /**
     * 지정된 쿼리 범위를 A1 스타일 표기법으로 반환.
     *
     * @return A1 스타일 표기법으로 변환된 쿼리 범위 문자열.
     * @see <a href=https://developers.google.com/workspace/sheets/api/guides/concepts?hl=ko#cell>A1 스타일 표기법</a>
     */
    public String buildRange() {
        StringBuilder query = new StringBuilder();

        // 시트 이름 정보
        query.append("'").append(sheetsInfo.getSheetName()).append("'");
        query.append("!");

        // 시작 열 및 행 번호
        query.append(startColumn);
        if (startRowNum != null && startRowNum > 0) {
            query.append(startRowNum);
        }
        query.append(":");

        // 마지막 열 및 행 번호
        query.append(endColumn);
        if (endRowNum != null && endRowNum > 0) {
            query.append(endRowNum);
        }

        return query.toString();
    }
}
