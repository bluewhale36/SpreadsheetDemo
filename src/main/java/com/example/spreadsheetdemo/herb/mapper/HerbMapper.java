package com.example.spreadsheetdemo.herb.mapper;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.herb.dto.HerbDTO;
import com.example.spreadsheetdemo.herb.dto.HerbLogDTO;
import com.example.spreadsheetdemo.herb.dto.HerbRegisterDTO;
import com.example.spreadsheetdemo.herb.dto.HerbUpdateDTO;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 약재 관련 DTO 와 GoogleSpreadSheetAPI 데이터 간의 매핑 처리 클래스.
 */
@Slf4j
@Component
public class HerbMapper {

    /**
     * GoogleSpreadSheetAPI 에서 조회한 약재 데이터를 {@code List<HerbDTO>} 로 변환.
     *
     * @param value GoogleSpreadSheetAPI 조회 반환값 {@link ValueRange}.
     * @return 매핑된 HerbDTO 리스트.
     * @throws IllegalArgumentException 시트 이름이 일치하지 않을 경우.
     * @throws NullPointerException 찾은 데이터의 범위가 {@code null} 값인 경우.
     */
    public List<HerbDTO> toHerbDTOList(ValueRange value) {

        if (value == null) { return null; }

        // ValueRange 의 시트 범위 정보 확인
        String range = value.getRange();

        if (range != null) {
            // ValueRange 의 시트 이름 확인
            String sheetName = range.split("!")[0];
            // 조회된 데이터의 시작 행 번호 (예시: "Herb!A2:D10" -> 2)
            Integer startRowNum = extractRowNumFromRange(range);

            if (sheetName.equals(SheetsInfo.HERB.getSheetName()) && startRowNum != null) {
                // 반환되는 리스트
                List<HerbDTO> herbDTOList = new ArrayList<>();
                // GoogleSpreadSheetAPI 에서 조회된 데이터
                List<List<Object>> values = value.getValues();

                if (!values.isEmpty()) {
                    // 데이터 매핑
                    // startRowNum 이 1인 경우 첫 번째 행은 헤더이므로 제외
                    for (int i = startRowNum == 1 ? 1 : 0; i < values.size(); i++) {
                        HerbDTO dto;
                        List<Object> row = values.get(i);

                        // 행 데이터의 크기에 따라 매핑 처리
                        switch (row.size()) {
                            // 아 이렇게 하드코딩 하기 싫다..ㅎ
                            case 2:
                                // 필수값만 있는 경우
                                dto = HerbDTO.builder()
                                        .rowNum(i +startRowNum)
                                        .name(row.get(0).toString())
                                        .amount(parseLong(row.get(1).toString()))
                                        .build();
                                break;
                            case 3:
                                // 메모가 없는 경우
                                dto = HerbDTO.builder()
                                        .rowNum(i +startRowNum)
                                        .name(row.get(0).toString())
                                        .amount(parseLong(row.get(1).toString()))
                                        .lastStoredDate(parseDate(row.get(2).toString()))
                                        .build();
                                break;
                            case 4:
                                // 모든 값이 있는 경우
                                dto = HerbDTO.builder()
                                        .rowNum(i +startRowNum)
                                        .name(row.get(0).toString())
                                        .amount(parseLong(row.get(1).toString()))
                                        .lastStoredDate(parseDate(row.get(2).toString()))
                                        .memo(row.get(3).toString())
                                        .build();
                                break;
                            default:
                                log.warn("Row data does not match expected schema: {}", row);
                                continue;
                        }
                        herbDTOList.add(dto);
                    }
                }
                return herbDTOList;
            } else {
                log.error("Sheet name does not match for HerbMapper: {}", sheetName);
                throw new IllegalArgumentException(String.format("잘못된 시트의 데이터 매핑을 시도했습니다: %s", sheetName));
            }
        } else {
            log.error("Retrieved data range is null.");
            throw new NullPointerException("데이터 매핑 도중 오류가 발생했습니다.");
        }
    }

    /**
     * 약재 등록 목적으로 HerbRegisterDTO 를 GoogleSpreadSheetAPI 에서 요구하는 형식으로 변환.
     * 
     * @param dto 변환할 HerbRegisterDTO 객체.
     * @return 변환된 데이터 리스트.
     */
    public List<List<Object>> fromHerbRegisterDTO(HerbRegisterDTO dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        
        List<Object> row = List.of(
                dto.getName(), dto.getAmount(), dto.getLastStoredDate().toString(), dto.getMemo()
        );
        return Collections.singletonList(row);
    }

    /**
     * <u>최신 값 갱신 목적</u>으로 HerbUpdateDTO 를 GoogleSpreadSheetAPI 에서 요구하는 형식으로 변환.<br/>
     * {@link HerbUpdateDTO} 의 {@link HerbUpdateDTO#newAmount}, {@link HerbUpdateDTO#newLastStoredDate}, {@link HerbUpdateDTO#newMemo} 필드 사용.
     *
     * @param dto 변환할 HerbUpdateDTO 객체.
     * @return 변환된 데이터 리스트.
     */
    public List<List<Object>> fromHerbUpdateDTOForUpdate(HerbUpdateDTO dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        
        List<Object> row = List.of(
                dto.getName(), dto.getNewAmount(), dto.getNewLastStoredDate().toString(), dto.getNewMemo()
        );
        return Collections.singletonList(row);
    }

    /**
     * <u>롤백 목적</u>으로 HerbUpdateDTO 를 GoogleSpreadSheetAPI 에서 요구하는 형식으로 변환.<br/>
     * {@link HerbUpdateDTO} 의 {@link HerbUpdateDTO#originalAmount}, {@link HerbUpdateDTO#originalLastStoredDate}, {@link HerbUpdateDTO#originalMemo} 필드 사용.
     *
     * @param dto 변환할 HerbUpdateDTO 객체.
     * @return 변환된 데이터 리스트.
     */
    public List<List<Object>> fromHerbUpdateDTOForRollback(HerbUpdateDTO dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        
        List<Object> row = List.of(
                dto.getName(), dto.getOriginalAmount(), dto.getOriginalLastStoredDate().toString(), dto.getOriginalMemo()
        );
        return Collections.singletonList(row);
    }

    /**
     * GoogleSpreadSheetAPI 에서 조회한 약재 수정 로그 데이터를 {@code List<HerbLogDTO>} 로 변환.
     *
     * @param value GoogleSpreadSheetAPI 조회 반환값 {@link ValueRange}.
     * @return 매핑된 HerbLogDTO 리스트.
     * @throws IllegalArgumentException 시트 이름이 일치하지 않을 경우.
     * @throws NullPointerException 찾은 데이터의 범위가 {@code null} 값인 경우.
     */
    public List<HerbLogDTO> toHerbLogDTOList(ValueRange value) {

        if (value == null) { return null; }

        // ValueRange 의 시트 범위 정보 확인
        String range = value.getRange();

        if (range != null) {
            // ValueRange 의 시트 이름 확인
            String sheetName = range.split("!")[0];

            if (sheetName.equals(SheetsInfo.HERB_LOG.getSheetName())) {
                // 반환되는 리스트
                List<HerbLogDTO> herbLogDTOList = new ArrayList<>();
                // GoogleSpreadSheetAPI 에서 조회된 데이터
                List<List<Object>> values = value.getValues();

                if (!values.isEmpty()) {
                    // 데이터 매핑
                    // 첫 번째 행은 헤더이므로 제외
                    values.remove(0);

                    for (List<Object> row : values) {
                        herbLogDTOList.add(
                                HerbLogDTO.builder()
                                        .loggedDatetime(parseDateTime(row.get(0).toString()))
                                        .name(row.get(1).toString())
                                        .beforeAmount(parseLong(row.get(2).toString()))
                                        .afterAmount(parseLong(row.get(3).toString()))
                                        .build()
                        );
                    }
                }
                return herbLogDTOList;
            } else {
                log.error("Sheet name does not match for HerbLogMapper: {}", sheetName);
                throw new IllegalArgumentException(String.format("잘못된 시트의 데이터 매핑을 시도했습니다: %s", sheetName));
            }
        } else {
            log.error("Retrieved data range is null.");
            throw new NullPointerException("데이터 매핑 도중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그 삽입 목적으로 HerbLogDTO 를 GoogleSpreadSheetAPI 에서 요구하는 형식으로 변환.
     *
     * @param dto 변환할 HerbLogDTO 객체.
     * @return 변환된 데이터 리스트.
     */
    public List<List<Object>> fromHerbLogDTO(HerbLogDTO dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        
        List<Object> row = List.of(
                dto.getLoggedDatetime().toString(), dto.getName(), dto.getBeforeAmount(), dto.getAfterAmount()
        );
        return Collections.singletonList(row);
    }

    private LocalDate parseDate(String dateStr) {

        DateTimeFormatter[] CANDIDATES = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy.M.d"),
                DateTimeFormatter.ofPattern("yyyy. M. d"),
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyyMd")
        };

        for (DateTimeFormatter f : CANDIDATES) {
            try {
                return LocalDate.parse(dateStr, f);
            } catch (Exception ignored) {
            }
        }
        log.error("Unavailable to parse date: {}", dateStr);
        return null;
    }

    private LocalDateTime parseDateTime(String datetimeStr) {

        DateTimeFormatter[] CANDIDATES = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy.M.d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy. M. d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"),
                DateTimeFormatter.ofPattern("yyyyMd H:m:s")
        };

        for (DateTimeFormatter f : CANDIDATES) {
            try {
                return LocalDateTime.parse(datetimeStr, f);
            } catch (Exception ignored) {
            }
        }
        log.error("Unavailable to parse datetime: {}", datetimeStr);
        return null;
    }

    private Long parseLong(String longStr) {
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            log.error("Error parsing long: {}", longStr, e);
            return null;
        }
    }

    private Integer extractRowNumFromRange(String range) {
        if (range == null) return null;
        // 시트 이름 뒤의 첫 번째 숫자 그룹을 찾음
        Matcher matcher = Pattern.compile("![A-Za-z]+(\\d+)").matcher(range);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
