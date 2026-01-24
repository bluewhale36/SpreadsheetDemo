package com.example.spreadsheetdemo.common.data;

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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class SheetsDataQueryObject {

    /*
        SELECT, UPDATE, DELETE, INSERT
     */

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

    public <T> List<T> select(SheetsQuerySpec querySpec, RowMapper<T> rowMapper) throws GeneralSecurityException, IOException {
        List<T> result;
        ValueRange value;
        try {
            String query = querySpec.getQueryRangeAsA1Notation();

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
            result.add(rowMapper.toEntity(data.get(i), startedRowNum +i));
        }

        return result;
    }

    public <T> String insert(T entity, SheetsQuerySpec querySpec, RowMapper<T> rowMapper) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(rowMapper.toRow(entity));

        AppendValuesResponse result = getSheetsService().spreadsheets().values()
                .append(SPREADSHEET_ID, querySpec.getQueryRangeAsA1Notation(), value)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();

        String insertedRange = result.getUpdates().getUpdatedRange();
        log.info("Data has been inserted at range: {}", insertedRange);

        return insertedRange;
    }

    public <T> String update(T entity, SheetsQuerySpec querySpec, RowMapper<T> rowMapper) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(rowMapper.toRow(entity));
        System.out.println(value.toPrettyString());

        UpdateValuesResponse result = getSheetsService()
                .spreadsheets()
                .values()
                .update(SPREADSHEET_ID, querySpec.getQueryRangeAsA1Notation(), value)
                .setValueInputOption("USER_ENTERED")
                .execute();

        String updatedRange = result.getUpdatedRange();
        log.info("Data has been updated at range: {}", updatedRange);

        return updatedRange;
    }

    public String delete(SheetsQuerySpec querySpec) throws GeneralSecurityException, IOException {
        ClearValuesResponse result = getSheetsService()
                .spreadsheets()
                .values()
                .clear(SPREADSHEET_ID, querySpec.getQueryRangeAsA1Notation(), new ClearValuesRequest())
                .execute();

        String deletedRange = result.getClearedRange();
        log.info("Data has been deleted at range: {}", deletedRange);

        return deletedRange;
    }

    private Integer extractRowNumFromRange(String range) {
        if (range == null) return null;
        // 시트 이름 뒤의 첫 번째 숫자 그룹을 찾음
        Matcher matcher = Pattern.compile("![A-Za-z]+(\\d+)").matcher(range);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
