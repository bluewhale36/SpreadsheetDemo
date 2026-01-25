package com.example.spreadsheetdemo.herb.domain.queryspec;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.enums.HerbSheetColumnInfo;
import lombok.Getter;

import java.util.function.Predicate;

@Getter
public class HerbQuerySpec extends SheetsQuerySpec<Herb> {

    private HerbQuerySpec(
            Integer startRowNum, Integer endRowNum, Predicate<Herb> queryCondition, HerbSheetColumnInfo... herbSheetColumnInfos
    ) {
        super(SheetsInfo.HERB, startRowNum, endRowNum, queryCondition, herbSheetColumnInfos);
    }

    private HerbQuerySpec(Predicate<Herb> queryCondition) {
        this(
                SheetsInfo.HERB.getStartRowNum(), SheetsInfo.HERB.getEndRowNum(),
                queryCondition, HerbSheetColumnInfo.values()
        );
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
     * @param queryCondition 데이터를 필터링 하는 조건.
     * @param herbSheetColumnInfos 조회할 열의 이름.
     * @return {@link SheetsInfo#HERB} 시트의 전달된 시작-종료 열 번호의 모든 데이터를 대상 범위로 하는 {@link HerbQuerySpec} 객체.
     */
    public static HerbQuerySpec ofAllRowDataRange(
            Predicate<Herb> queryCondition, HerbSheetColumnInfo... herbSheetColumnInfos
    ) {
        if (herbSheetColumnInfos == null || herbSheetColumnInfos.length == 0) {
            return new HerbQuerySpec(
                    SheetsInfo.HERB.getStartRowNum(), SheetsInfo.HERB.getEndRowNum(), queryCondition, HerbSheetColumnInfo.values()
            );
        }
        return new HerbQuerySpec(
                SheetsInfo.HERB.getStartRowNum(), SheetsInfo.HERB.getEndRowNum(), queryCondition, herbSheetColumnInfos
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
    public static HerbQuerySpec ofAllColumnDataRange(
            int startRowNum, Integer endRowNum, Predicate<Herb> queryCondition
    ) {
        return new HerbQuerySpec(startRowNum, endRowNum, queryCondition, HerbSheetColumnInfo.values());
    }
}
