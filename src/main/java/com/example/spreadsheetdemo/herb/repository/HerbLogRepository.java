package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * 약재 수량 로그 정보가 저장된 Google Spreadsheet API 연동 Repository.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HerbLogRepository {

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

    /**
     * 약재 로그가 담긴 스프레드시트의 모든 행을 조회.
     *
     * @return 스프레드시트의 모든 행 정보 {@link ValueRange}
     * @throws IOException on Credentials file read exception.
     * @throws GeneralSecurityException on security exception.
     */
    public ValueRange selectAll() throws IOException, GeneralSecurityException {
        ValueRange result = null;
        try {
            // Create the sheets API client
            Sheets service = getSheetsService();
            // 전체 데이터 조회
            result = service.spreadsheets()
                    .values()
                    .get(SPREADSHEET_ID, SheetsInfo.HERB_LOG.getDataRange())
                    .execute();
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                log.error("Spreadsheet not found with id {}", SPREADSHEET_ID);
            } else {
                throw e;
            }
        } catch (IOException | GeneralSecurityException e) {
            log.error("Credential Error occurred while accessing Google Sheets API.");
            throw e;
        }
        return result;
    }

    /**
     * 약재 로그를 스프레드시트에 삽입.
     *
     * @param content 삽입할 약재 로그 정보 리스트
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    public void insertLog(List<List<Object>> content) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(content);

        AppendValuesResponse result = getSheetsService().spreadsheets().values()
                // 해당 스프레드시트 데이터 범위 명시할 경우 자동으로 마지막에 데이터 삽입됨
                .append(SPREADSHEET_ID, SheetsInfo.HERB_LOG.getDataRange(), value)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")     // 필요 시 행 삽입
                .execute();

        String insertedRange = result.getUpdates().getUpdatedRange();  // "Sheet1!A21:C22" 형태로 반환
        log.info("Log inserted at range: {}", insertedRange);
    }
}
