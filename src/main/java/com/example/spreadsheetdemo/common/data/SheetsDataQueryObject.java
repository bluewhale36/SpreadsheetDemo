package com.example.spreadsheetdemo.common.data;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;
import com.example.spreadsheetdemo.common.util.RowMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class SheetsDataQueryObject {

    @Value("${google.spreadsheet.id}")
    private String SPREADSHEET_ID;

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/spreadsheet-test.json";
    private Sheets sheetsService;

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        if (sheetsService == null) {
            GoogleCredentials credential = GoogleCredentials
                    .fromStream(new ClassPathResource(CREDENTIALS_FILE_PATH).getInputStream())
                    .createScoped("https://www.googleapis.com/auth/spreadsheets");
            sheetsService = new Sheets
                    .Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credential)
            )
                    .setApplicationName("uniflee")
                    .build();
        }
        return sheetsService;
    }

    // 시트 ID 캐싱
    private final Map<String, Integer> sheetIdCache = new ConcurrentHashMap<>();

    /**
     * Google Spreadsheet 의 sheetId 반환.
     *
     * @param sheetName 반환할 sheetId 의 대상 시트 명.
     * @return {@link Integer} 타입의 sheetId.
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     * @throws IllegalArgumentException {@code sheetName} 에 대한 시트를 찾을 수 없을 경우 발생.
     */
    private Integer getSheetId(String sheetName) throws GeneralSecurityException, IOException {
        // 1. 캐시에 있으면 바로 반환
        if (sheetIdCache.containsKey(sheetName)) {
            return sheetIdCache.get(sheetName);
        }

        // 2. 캐시에 없으면 API 호출하여 메타데이터 조회
        Spreadsheet spreadsheet = getSheetsService().spreadsheets()
                .get(SPREADSHEET_ID)
                .execute();

        // 3. 모든 시트를 순회하며 이름이 일치하는 시트의 ID를 찾음
        for (Sheet sheet : spreadsheet.getSheets()) {
            String title = sheet.getProperties().getTitle();
            Integer sheetId = sheet.getProperties().getSheetId();

            // 캐시에 저장
            sheetIdCache.put(title, sheetId);

            if (title.equals(sheetName)) {
                return sheetId;
            }
        }

        throw new IllegalArgumentException("Unavailable to find sheetId for sheet name of: " + sheetName);
    }

    /*
        ====================================================
            CRUD LOGIC STARTS FROM HERE
        ====================================================
     */

    public <E extends SheetsEntity> List<E> select(SheetsQuerySpec<E> querySpec, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {
        List<E> result;
        ValueRange value;
        try {
            String query = querySpec.buildRange();

            // Create the sheets API client
            Sheets service = getSheetsService();
            // 전체 데이터 조회
            value = service.spreadsheets()
                    .values()
                    .get(SPREADSHEET_ID, query)
                    .execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error("Credential Error occurred while accessing Google Sheets API.");
            throw e;
        }

        String selectedRange = value.getRange();
        List<List<Object>> data = value.getValues();

        Integer startedRowNum = extractRowNumFromRange(selectedRange);
        if (startedRowNum == null) {
            throw new IllegalStateException("조회 오류");
        }

        result = new ArrayList<>();
        if (data != null) {

            for (int i = 0; i < data.size(); i++) {
                if (data.get(i) == null || data.get(i).isEmpty()) continue;

                E entity = rowMapper.toEntity(data.get(i), startedRowNum +i, querySpec);
                if (querySpec.matches(entity)) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    public <E extends SheetsEntity> E insert(E entity, SheetsQuerySpec<E> querySpec, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(rowMapper.toRow(entity));

        AppendValuesResponse result = getSheetsService().spreadsheets().values()
                .append(SPREADSHEET_ID, querySpec.buildRange(), value)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();

        String insertedRange = result.getUpdates().getUpdatedRange();
        log.info("Data has been inserted at range: {}", insertedRange);

        return entity;
    }

    public <E extends SheetsEntity> List<E> insertAll(List<E> entityList, SheetsInfo sheetsInfo, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }

        List<List<Object>> values = new ArrayList<>();
        for (E entity : entityList) {
            values.addAll(rowMapper.toRow(entity));
        }

        ValueRange value = new ValueRange().setValues(values);

        AppendValuesResponse result = getSheetsService().spreadsheets().values()
                .append(SPREADSHEET_ID, sheetsInfo.getDataRange(), value)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();

        String updatedRange = result.getUpdates().getUpdatedRange();
        log.info("Batch insert completed. Range: {}", updatedRange);

        return entityList;
    }

    public <E extends SheetsEntity> E update(E entity, SheetsQuerySpec<E> querySpec, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(rowMapper.toRow(entity));

        UpdateValuesResponse result = getSheetsService()
                .spreadsheets()
                .values()
                .update(SPREADSHEET_ID, querySpec.buildRange(), value)
                .setValueInputOption("USER_ENTERED")
                .execute();

        String updatedRange = result.getUpdatedRange();
        log.info("Data has been updated at range: {}", updatedRange);

        return entity;
    }

    /**
     * 여러 개의 Entity 를 한 번의 API 호출로 일괄 수정합니다.
     * <p>각 Entity 는 반드시 {@code rowNum} 을 가지고 있어야 합니다.</p>
     *
     * @param entityList 수정할 내용이 반영된 Entity 리스트.
     * @param sheetsInfo 대상 시트 정보.
     * @param rowMapper Entity 를 Row 로 변환할 매퍼.
     * @return 업데이트된 행의 개수.
     */
    public <E extends SheetsEntity> List<E> updateAll(List<E> entityList, SheetsInfo sheetsInfo, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }

        // 1. ValueRange 리스트 생성
        List<ValueRange> updates = new ArrayList<>();

        for (E entity : entityList) {
            // 각 엔티티의 rowNum 을 이용해 개별 범위(A1 Notation) 생성
            String specificRange = sheetsInfo.getSpecificRowNum(entity.getRowNum());

            // Row 데이터 변환
            List<List<Object>> rowData = rowMapper.toRow(entity);

            updates.add(new ValueRange()
                                .setRange(specificRange)
                                .setValues(rowData));
        }

        // Batch Update Request 객체 생성
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(updates);

        // API 호출
        BatchUpdateValuesResponse response = getSheetsService().spreadsheets().values()
                .batchUpdate(SPREADSHEET_ID, body)
                .execute();

        log.info("Batch update completed. Total updated rows: {}", response.getTotalUpdatedRows());
        return entityList;
    }

    /**
     * Google Spreadsheet 에서 조건에 맞거나 지정된 행을 삭제합니다.
     *
     * <p>이 메서드는 {@link SheetsQuerySpec} 의 구성에 따라 두 가지 모드로 동작합니다:</p>
     * <ol>
     * <li><b>최적화된 단건 삭제 (Optimized Direct Delete):</b>
     * <br>{@code queryCondition} 이 {@code null} 이고, 시작 행({@code startRowNum})과 종료 행({@code endRowNum})이 동일하게 지정된 경우,
     * 데이터 조회(Select) 과정을 생략하고 즉시 삭제 API 를 호출합니다.
     * <br><b>주의:</b> 이 경우 삭제된 데이터를 조회하지 않으므로 <b>빈 리스트(Empty List)</b>가 반환됩니다.</li>
     *
     * <li><b>조건부 검색 삭제 (Conditional Search & Delete):</b>
     * <br>위의 경우가 아니라면, 먼저 데이터를 조회하여 {@code queryCondition} 에 부합하는 대상을 필터링합니다.
     * 이후 대상 행 번호들을 내림차순으로 정렬하여 일괄 삭제(Batch Update)를 수행합니다.
     * <br>이 경우 삭제된 엔티티들의 리스트가 반환됩니다.</li>
     * </ol>
     *
     * @param querySpec 삭제할 대상의 범위(물리적 위치) 또는 필터링 조건(논리적 조건)을 담은 명세 객체.
     * @param rowMapper 시트 데이터를 엔티티로 변환하기 위한 매퍼 (검색 삭제 모드에서 사용).
     * @return 삭제된 엔티티의 리스트. (단, <b>최적화된 단건 삭제</b> 수행 시에는 빈 리스트 반환)
     * @param <E> {@link SheetsEntity} 를 상속하는 Entity Class.
     * @throws GeneralSecurityException Google API 인증/권한 관련 예외 발생 시.
     * @throws IOException 네트워크 통신 오류 또는 입출력 예외 발생 시.
     */
    public <E extends SheetsEntity> List<E> delete(SheetsQuerySpec<E> querySpec, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {

        List<E> targets;
        List<Integer> rowsToDelete = new ArrayList<>();

        if (
                querySpec.getQueryCondition() == null &&
                querySpec.getStartRowNum() != null &&
                querySpec.getStartRowNum().equals(querySpec.getEndRowNum())
        ) {
            rowsToDelete.add(querySpec.getStartRowNum());
            targets = List.of();
        } else {
            // 삭제 대상 조회
            targets = this.select(querySpec, rowMapper);

            if (targets.isEmpty()) {
                return List.of();
            }

            // 삭제 대상 객체의 행번호 내림차순 정렬
            rowsToDelete = targets.stream()
                    .map(SheetsEntity::getRowNum)
                    .sorted(Comparator.reverseOrder())
                    .toList();
        }

        Integer sheetId = getSheetId(querySpec.getSheetsInfo().getSheetName());

        // Request 객체 생성
        List<Request> requests = new ArrayList<>();
        for (Integer rowNum : rowsToDelete) {
            int startIndex = rowNum - 1;

            requests.add(new Request().setDeleteDimension(
                    new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                              .setSheetId(sheetId)
                                              .setDimension("ROWS")
                                              .setStartIndex(startIndex)
                                              .setEndIndex(startIndex + 1)
                            )
            ));
        }

        // Batch Update 실행
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        getSheetsService().spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();

        return targets;
    }

    private Integer extractRowNumFromRange(String range) {
        if (range == null) return null;
        // 시트 이름 뒤의 첫 번째 숫자 그룹을 찾음
        Matcher matcher = Pattern.compile("![A-Za-z]+(\\d+)").matcher(range);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
