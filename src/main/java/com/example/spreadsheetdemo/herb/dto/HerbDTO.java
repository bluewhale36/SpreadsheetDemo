package com.example.spreadsheetdemo.herb.dto;

import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import lombok.*;

import java.time.LocalDate;

/**
 * 약재 정보 조회용 DTO
 */
@Getter
@Builder
@RequiredArgsConstructor // Controller 의 @RequestBody 바인딩 목적
@ToString
@EqualsAndHashCode
public class HerbDTO {

    private final Integer rowNum;
    private final String name;
    private final Long amount;
    private final LocalDate lastStoredDate;
    private final String memo;

    public static HerbDTO from(HerbUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        return HerbDTO.builder()
                .rowNum(dto.getRowNum())
                .name(dto.getName())
                .amount(dto.getOriginalAmount())
                .lastStoredDate(dto.getOriginalLastStoredDate())
                .memo(dto.getOriginalMemo())
                .build();
    }

    public static HerbDTO from(Herb herb) {
        if (herb == null) return null;

        return HerbDTO.builder()
                .rowNum(herb.getRowNum())
                .name(herb.getName())
                .amount(herb.getAmount())
                .lastStoredDate(herb.getLastStoredDate())
                .memo(herb.getMemo())
                .build();
    }
}
