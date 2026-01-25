package com.example.spreadsheetdemo.common.domain.queryspec;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.common.enums.SheetColumnInfo; // 가정된 패키지 경로
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * <h3>구글 스프레드시트 동적 조회 명세서 (Dynamic Data Query Specification)</h3>
 *
 * <p>이 클래스는 구글 시트 API 요청을 위한 <b>물리적 범위(Range)</b>와
 * 메모리 상에서의 <b>논리적 조건(Filter)</b>을 정의합니다.</p>
 *
 * <p><b>주요 특징 및 변경점:</b></p>
 * <ul>
 * <li><b>컬럼 기반 범위 자동 계산:</b> 생성자 시점에 조회하려는 컬럼 정보들({@link SheetColumnInfo})을 전달받습니다.
 * 내부적으로 이 컬럼들을 정렬하여 <b>"가장 앞선 컬럼(Start Column)"</b>과 <b>"가장 뒤선 컬럼(End Column)"</b>을 찾아
 * API 요청 범위를 자동으로 결정합니다.</li>
 * <li><b>동적 매핑 지원:</b> {@link #targetColumnList} 를 통해 어떤 컬럼들이 요청되었는지 {@code RowMapper} 에게 정보를 제공함으로써,
 * 전체 열을 조회하지 않고 부분적인 열만 조회했을 때도 정확한 데이터 매핑을 가능하게 합니다.</li>
 * </ul>
 *
 * @param <ENTITY> 이 명세서가 다루는 {@link SheetsEntity} 의 구체적인 타입.
 */
@Getter
public abstract class SheetsQuerySpec<ENTITY extends SheetsEntity> {

    /**
     * 대상 시트의 메타정보 (시트 이름, 시트 ID 등).
     */
    protected final SheetsInfo sheetsInfo;

    /**
     * 조회 대상이 되는 컬럼들의 목록.
     * <br>생성자에서 전달된 컬럼들이 시트 상의 순서대로 정렬되어 저장됩니다.
     * <br>{@code RowMapper} 에서 데이터를 엔티티에 매핑할 때 인덱스 기준이 됩니다.
     */
    protected final List<? extends SheetColumnInfo> targetColumnList;

    /**
     * 계산된 조회 시작 컬럼 (예: "A").
     * <br>{@link #targetColumnList} 중 가장 앞선 컬럼입니다.
     */
    protected final String startColumn;

    /**
     * 계산된 조회 종료 컬럼 (예: "D").
     * <br>{@link #targetColumnList} 중 가장 뒤에 있는 컬럼입니다.
     */
    protected final String endColumn;

    protected final Integer startRowNum;
    protected final Integer endRowNum;

    /**
     * 메모리 상에서 수행할 논리적 필터링 조건.
     * <br>{@code null} 일 경우 모든 데이터를 대상으로 합니다.
     */
    protected final Predicate<ENTITY> queryCondition;

    /**
     * 컬럼 정보를 기반으로 조회 범위를 계산하여 명세서를 생성합니다.
     *
     * @param sheetsInfo      대상 시트 정보 (필수).
     * @param startRowNum     조회 시작 행 번호 (Nullable).
     * @param endRowNum       조회 종료 행 번호 (Nullable). {@code null}일 경우 시트의 마지막 데이터까지 조회.
     * @param queryCondition  데이터 필터링 조건 (Nullable).
     * @param targetColumns   조회할 컬럼 정보 목록 (가변 인자, 필수). 하나 이상의 컬럼이 지정되어야 합니다.
     * @throws IllegalArgumentException {@code sheetsInfo} 가 {@code null} 이거나, {@code targetColumns} 가 {@code null} 또는 비어있는 경우.
     */
    protected SheetsQuerySpec(
            SheetsInfo sheetsInfo,
            Integer startRowNum, Integer endRowNum,
            Predicate<ENTITY> queryCondition,
            SheetColumnInfo... targetColumns
    ) {
        if (sheetsInfo == null) throw new IllegalArgumentException("sheetsInfo cannot be null");
        if (startRowNum == null && endRowNum != null) throw new IllegalArgumentException("endRowNum should be null when startRowNum is null.");
        if (targetColumns == null || targetColumns.length == 0) throw new IllegalArgumentException("targetColumns cannot be null or empty");

        this.sheetsInfo = sheetsInfo;

        // 전달받은 컬럼들을 시트 상의 순서(A, B, C...)대로 정렬하여 리스트로 변환
        this.targetColumnList = Arrays.stream(targetColumns).sorted(Comparator.comparing(SheetColumnInfo::getColumnInSheet)).toList();

        // 정렬된 리스트의 양 끝단을 이용하여 물리적 조회 범위(Range) 계산
        this.startColumn = this.targetColumnList.get(0).getColumnInSheet();
        this.endColumn = this.targetColumnList.get(this.targetColumnList.size() - 1).getColumnInSheet();

        this.startRowNum = startRowNum;
        this.endRowNum = endRowNum == null ? sheetsInfo.getEndRowNum() : endRowNum;

        this.queryCondition = queryCondition;
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
     * 계산된 컬럼 범위와 지정된 행 범위를 조합하여 A1 스타일 표기법을 반환합니다.
     * <br>예: {@code 'Sheet1'!A2:D100}
     *
     * @return A1 스타일 표기법으로 변환된 쿼리 범위 문자열.
     * @see <a href=https://developers.google.com/workspace/sheets/api/guides/concepts?hl=ko#cell>A1 스타일 표기법</a>
     */
    public String buildRange() {
        StringBuilder query = new StringBuilder();

        // 시트 이름 정보
        query.append("'").append(sheetsInfo.getSheetName()).append("'");
        query.append("!");

        // 시작 열 및 행 번호 (계산된 startColumn 사용)
        query.append(startColumn);
        if (startRowNum != null && startRowNum > 0) {
            query.append(startRowNum);
        }
        query.append(":");

        // 마지막 열 및 행 번호 (계산된 endColumn 사용)
        query.append(endColumn);
        if (endRowNum != null && endRowNum > 0) {
            query.append(endRowNum);
        }

        return query.toString();
    }
}