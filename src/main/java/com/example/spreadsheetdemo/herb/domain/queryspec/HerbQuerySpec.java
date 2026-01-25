package com.example.spreadsheetdemo.herb.domain.queryspec;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;

import java.util.function.Predicate;

public class HerbQuerySpec extends SheetsQuerySpec<Herb> {

    private HerbQuerySpec(
            String startColumn, String endColumn, Integer startRowNum, Integer endRowNum, Predicate<Herb> queryCondition
    ) {
        super(SheetsInfo.HERB, startColumn, endColumn, startRowNum, endRowNum, queryCondition);
    }

    private HerbQuerySpec(Predicate<Herb> queryCondition) {
        this(SheetsInfo.HERB.getStartColumn(), SheetsInfo.HERB.getEndColumn(), SheetsInfo.HERB.getStartRowNum(), SheetsInfo.HERB.getEndRowNum(), queryCondition);
    }

    /**
     * {@link SheetsInfo#HERB} 시트의 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체를 반환함.
     *
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB} 시트의 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체.
     */
    public static HerbQuerySpec ofAllDataRange(Predicate<Herb> queryCondition) {
        return new HerbQuerySpec(queryCondition);
    }

    /**
     * {@link SheetsInfo#HERB} 시트의 특정 열 범위에 대한 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체를 반환함.
     *
     * @param startColumn 시작 열 번호 문자열.
     * @param endColumn 종료 열 번호 문자열. {@code null} 값 전달 시, {@code SheetsInfo.HERB.endColumn} 까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB} 시트의 전달된 시작-종료 열 번호의 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체.
     */
    public static HerbQuerySpec ofAllRowDataWithSpecificColumnRange(
            String startColumn, String endColumn, Predicate<Herb> queryCondition
    ) {
        return new HerbQuerySpec(
                startColumn, endColumn,
                SheetsInfo.HERB.getStartRowNum(), SheetsInfo.HERB.getEndRowNum(),
                queryCondition
        );
    }

    /**
     * {@link SheetsInfo#HERB} 시트의 특정 행 범위에 대한 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체를 반환함.
     *
     * @param startRowNum 시작 행 번호.
     * @param endRowNum 종료 행 번호. {@code null} 값 전달 시, {@code SheetsInfo.HERB.endRowNum} 까지의 범위로 지정되거나, 시작 행 번호 이후의 유효한 모든 행 번호까지의 범위로 지정됨.
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @return {@link SheetsInfo#HERB} 시트의 전달된 시작-종료 행 번호의 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체.
     */
    public static HerbQuerySpec ofAllColumnDataWithSpecificRowRange(
            int startRowNum, Integer endRowNum, Predicate<Herb> queryCondition
    ) {
        return new HerbQuerySpec(
                SheetsInfo.HERB.getStartColumn(), SheetsInfo.HERB.getEndColumn(),
                startRowNum, endRowNum,
                queryCondition
        );
    }
}
