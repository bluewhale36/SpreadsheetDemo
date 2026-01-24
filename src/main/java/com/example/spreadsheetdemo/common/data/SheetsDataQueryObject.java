package com.example.spreadsheetdemo.common.data;

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

        throw new IllegalArgumentException("시트를 찾을 수 없습니다: " + sheetName);
    }

    /*
        ====================================================
            CRUD LOGIC STARTS FROM HERE
        ====================================================
     */

    public <T extends SheetsEntity> List<T> select(SheetsQuerySpec<T> querySpec, RowMapper<T> rowMapper) throws GeneralSecurityException, IOException {
        List<T> result;
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
        for (int i = 0; i < data.size(); i++) {
            T entity = rowMapper.toEntity(data.get(i), startedRowNum +i);
            if (querySpec.matches(entity)) {
                result.add(entity);
            }
        }

        return result;
    }

    public <T extends SheetsEntity> String insert(T entity, SheetsQuerySpec<T> querySpec, RowMapper<T> rowMapper) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(rowMapper.toRow(entity));

        AppendValuesResponse result = getSheetsService().spreadsheets().values()
                .append(SPREADSHEET_ID, querySpec.buildRange(), value)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();

        String insertedRange = result.getUpdates().getUpdatedRange();
        log.info("Data has been inserted at range: {}", insertedRange);

        return insertedRange;
    }

    public <T extends SheetsEntity> String update(T entity, SheetsQuerySpec<T> querySpec, RowMapper<T> rowMapper) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(rowMapper.toRow(entity));

        UpdateValuesResponse result = getSheetsService()
                .spreadsheets()
                .values()
                .update(SPREADSHEET_ID, querySpec.buildRange(), value)
                .setValueInputOption("USER_ENTERED")
                .execute();

        String updatedRange = result.getUpdatedRange();
        log.info("Data has been updated at range: {}", updatedRange);

        return updatedRange;
    }

    public <T extends SheetsEntity> String delete(SheetsQuerySpec<T> querySpec) throws GeneralSecurityException, IOException {
        ClearValuesResponse result = getSheetsService()
                .spreadsheets()
                .values()
                .clear(SPREADSHEET_ID, querySpec.buildRange(), new ClearValuesRequest())
                .execute();

        String deletedRange = result.getClearedRange();
        log.info("Data has been deleted at range: {}", deletedRange);

        return deletedRange;
    }

    /**
     * 조건에 맞는 행을 Google Spreadsheet 에서 삭제.<br/>
     * 해당 조건은 {@link SheetsQuerySpec#queryCondition} 에 명시되어 전달됨.
     * 해당 객체가 {@code null} 일 경우 전체 데이터가 삭제됨.
     *
     * @param querySpec 삭제할 조건을 담은 {@link SheetsQuerySpec} 객체.
     * @param rowMapper API 에서 반환된 결과를 {@link SheetsEntity} 상속체로 변환하는 매핑 객체.
     * @return 삭제된 행의 개수.
     * @param <E> {@link SheetsEntity} 를 상속하는 Entity Class.
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    public <E extends SheetsEntity> int delete(SheetsQuerySpec<E> querySpec, RowMapper<E> rowMapper) throws GeneralSecurityException, IOException {

        // 1. [Select & Filter] 삭제 대상을 조회 (기존 select 메서드 활용)
        List<E> targets = this.select(querySpec, rowMapper);

        if (targets.isEmpty()) {
            return 0;
        }

        // 2. [Sheet ID 조회] 시트 이름으로 ID 가져오기 (여기서 사용!)
        Integer sheetId = getSheetId(querySpec.getSheetsInfo().getSheetName());

        // 3. [Sort] 행 번호 내림차순 정렬 (아래부터 지워야 인덱스가 안 꼬임)
        List<Integer> rowsToDelete = targets.stream()
                .map(SheetsEntity::getRowNum)
                .sorted(Comparator.reverseOrder())
                .toList();

        // 4. [Request 생성] 삭제 요청 만들기
        List<Request> requests = new ArrayList<>();
        for (Integer rowNum : rowsToDelete) {
            // 구글 API 행 인덱스는 0부터 시작하므로 -1 (헤더가 1행이면 데이터는 2행부터지만 index는 1)
            // 주의: rowNum이 1-based라면 (rowNum - 1)이 정확한 인덱스입니다.
            int startIndex = rowNum - 1;

            requests.add(new Request().setDeleteDimension(
                    new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                              .setSheetId(sheetId) // 구한 ID 주입
                                              .setDimension("ROWS")
                                              .setStartIndex(startIndex)
                                              .setEndIndex(startIndex + 1)
                            )
            ));
        }

        // 5. [Execute] 실행
        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        getSheetsService().spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();

        return rowsToDelete.size();
    }

    private Integer extractRowNumFromRange(String range) {
        if (range == null) return null;
        // 시트 이름 뒤의 첫 번째 숫자 그룹을 찾음
        Matcher matcher = Pattern.compile("![A-Za-z]+(\\d+)").matcher(range);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
