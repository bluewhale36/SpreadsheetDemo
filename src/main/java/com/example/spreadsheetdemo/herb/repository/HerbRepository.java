package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import java.util.List;

/**
 * 약재 정보가 저장된 Google Spreadsheet API 연동 Repository.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HerbRepository {

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
     * 약재 정보가 담긴 스프레드시트의 모든 행을 조회.
     *
     * @return 스프레드시트의 모든 행 정보 {@link ValueRange}
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    public ValueRange selectAll() throws IOException, GeneralSecurityException {
        ValueRange result = null;
        try {
            // Create the sheets API client
            Sheets service = getSheetsService();
            // 전체 데이터 조회
            result = service.spreadsheets()
                    .values()
                    .get(SPREADSHEET_ID, SheetsInfo.HERB.getDataRange())
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

    public ValueRange selectByRange(String range) throws IOException, GeneralSecurityException {
        ValueRange result = null;
        try {
            // Create the sheets API client
            Sheets service = getSheetsService();
            // 전체 데이터 조회
            result = service.spreadsheets()
                    .values()
                    .get(SPREADSHEET_ID, range)
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
     * 약재 정보를 스프레드시트에 삽입.
     * 
     * @param content 삽입할 약재 정보 리스트
     * @return 삽입된 범위 문자열
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    public String insertHerb(List<List<Object>> content) throws GeneralSecurityException, IOException {
        ValueRange value = new ValueRange().setValues(content);
        
        AppendValuesResponse result = getSheetsService().spreadsheets().values()
                .append(SPREADSHEET_ID, SheetsInfo.HERB.getDataRange(), value)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
        
        String insertedRange = result.getUpdates().getUpdatedRange();
        log.info("Herb inserted at range: {}", insertedRange);
        
        return insertedRange;
    }

    /**
     * 약재 정보를 지정된 범위에 따라 스프레드시트에 업데이트.
     * 
     * @param range 업데이트할 범위 (예: "Sheet1!A2:D2")
     * @param content 업데이트할 약재 정보 리스트
     * @return 업데이트된 범위 문자열
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    public String updateByRange(String range, List<List<Object>> content) throws GeneralSecurityException, IOException {

        ValueRange value = new ValueRange().setValues(content);

        try {
            UpdateValuesResponse result = getSheetsService()
                    .spreadsheets()
                    .values()
                    .update(SPREADSHEET_ID, range, value)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            
            String updatedRange = result.getUpdatedRange();
            log.info("Herb Spreadsheet updated at range: {}", updatedRange);
            return updatedRange;
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            log.error("Error updating spreadsheet: {} - {}", error.getCode(), error.getMessage());
            throw e;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Credential Error occurred while accessing Google Sheets API.");
            throw e;
        }
    }

    /**
     * 지정된 범위의 약재 정보를 스프레드시트에서 삭제.
     * 
     * @param range 삭제할 범위 (예: "Sheet1!A2:D2")
     * @return 삭제된 범위 문자열
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    public String deleteByRange(String range) throws GeneralSecurityException, IOException {
        ClearValuesResponse result = getSheetsService()
                .spreadsheets()
                .values()
                .clear(SPREADSHEET_ID, range, new ClearValuesRequest())
                .execute();

        String deletedRange = result.getClearedRange();
        log.info("Herb Spreadsheet cleared at range: {}", deletedRange);
        return deletedRange;
    }

}
