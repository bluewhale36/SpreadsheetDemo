package com.example.spreadsheetdemo.herb.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * 약재 정보 신규 등록용 DTO
 */
@Getter
@Builder
@RequiredArgsConstructor // Controller 의 @RequestBody 바인딩 목적
@ToString
@EqualsAndHashCode
public class HerbRegisterDTO {

    private final String name;
    private final Long amount;
    private final LocalDate lastStoredDate;
    private final String memo;
}
