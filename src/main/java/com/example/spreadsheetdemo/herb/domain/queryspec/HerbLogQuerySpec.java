package com.example.spreadsheetdemo.herb.domain.queryspec;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;

import java.util.function.Predicate;

public class HerbLogQuerySpec extends SheetsQuerySpec<HerbLog> {

    private HerbLogQuerySpec(
            String startColumn, String endColumn, Integer startRowNum, Integer endRowNum, Predicate<HerbLog> queryCondition
    ) {
        super(SheetsInfo.HERB_LOG, startColumn, endColumn, startRowNum, endRowNum, queryCondition);
    }

    private HerbLogQuerySpec(Predicate<HerbLog> queryCondition) {
        this(SheetsInfo.HERB_LOG.getStartColumn(), SheetsInfo.HERB_LOG.getEndColumn(), SheetsInfo.HERB_LOG.getStartRowNum(), SheetsInfo.HERB_LOG.getEndRowNum(), queryCondition);
    }

    /**
     * {@link SheetsInfo#HERB_LOG} 시트의 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체를 반환함.
     *
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB_LOG} 시트의 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체.
     */
    public static HerbLogQuerySpec ofAllDataRange(Predicate<HerbLog> queryCondition) {
        return new HerbLogQuerySpec(queryCondition);
    }

    /**
     * {@link SheetsInfo#HERB_LOG} 시트의 특정 열 범위에 대한 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체를 반환함.
     *
     * @param startColumn 시작 열 번호 문자열.
     * @param endColumn 종료 열 번호 문자열. {@code null} 값 전달 시, {@code SheetsInfo.HERB_LOG.endColumn} 까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB_LOG} 시트의 전달된 시작-종료 열 번호의 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체.
     */
    public static HerbLogQuerySpec ofAllRowDataWithSpecificColumnRange(
            String startColumn, String endColumn, Predicate<HerbLog> queryCondition
    ) {
        return new HerbLogQuerySpec(
                startColumn, endColumn,
                SheetsInfo.HERB_LOG.getStartRowNum(), SheetsInfo.HERB_LOG.getEndRowNum(),
                queryCondition
        );
    }

    /**
     * {@link SheetsInfo#HERB_LOG} 시트의 특정 행 범위에 대한 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체를 반환함.
     *
     * @param startRowNum 시작 행 번호.
     * @param endRowNum 종료 행 번호. {@code null} 값 전달 시, {@code SheetsInfo.HERB_LOG.endRowNum} 까지의 범위로 지정되거나, 시작 행 번호 이후의 유효한 모든 행 번호까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB_LOG} 시트의 전달된 시작-종료 행 번호의 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체.
     */
    public static HerbLogQuerySpec ofAllColumnDataWithSpecificRowRange(
            int startRowNum, Integer endRowNum, Predicate<HerbLog> queryCondition
    ) {
        return new HerbLogQuerySpec(
                SheetsInfo.HERB_LOG.getStartColumn(), SheetsInfo.HERB_LOG.getEndColumn(),
                startRowNum, endRowNum,
                queryCondition
        );
    }

    /**
     * {@link SheetsInfo#HERB_LOG} 시트의 특정 행렬 범위에 대한 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체를 반환함.
     *
     * @param startColumn 시작 열 번호 문자열.
     * @param endColumn 종료 열 번호 문자열. {@code null} 값 전달 시, {@code SheetsInfo.HERB_LOG.endColumn} 까지의 범위로 지정됨.
     * @param startRowNum 시작 행 번호.
     * @param endRowNum 종료 행 번호. {@code null} 값 전달 시, {@code SheetsInfo.HERB_LOG.endRowNum} 까지의 범위로 지정되거나, 시작 행 번호 이후의 유효한 모든 행 번호까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB_LOG} 시트의 전달된 시작-종료 행렬 번호의 모든 데이터를 대상 범위로 하는 {@link HerbLogQuerySpec} 객체.
     */
    public static HerbLogQuerySpec ofAllDataWithSpecificDimensionRange(
            String startColumn, String endColumn, int startRowNum, Integer endRowNum, Predicate<HerbLog> queryCondition
    ) {
        return new HerbLogQuerySpec(
                startColumn, endColumn, startRowNum, endRowNum, queryCondition
        );
    }
}
