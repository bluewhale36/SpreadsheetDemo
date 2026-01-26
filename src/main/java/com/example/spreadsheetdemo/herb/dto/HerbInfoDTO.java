package com.example.spreadsheetdemo.herb.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class HerbInfoDTO {

    private final HerbDTO herbDTO;
    private final List<HerbLogDTO> herbLogDTOList;

    public static HerbInfoDTO of(HerbDTO herbDTO, List<HerbLogDTO> herbLogDTOList) {
        String herbName = herbDTO.getName();

        if (herbLogDTOList == null || herbLogDTOList.isEmpty()) {
            return HerbInfoDTO.builder().herbDTO(herbDTO).herbLogDTOList(List.of()).build();
        }

        if (
                // herbLogDTOList 에 herbDTO 에 제공된 약재 정보 외 다른 약재의 로그 데이터가 있을 경우
                !herbLogDTOList.stream().map(HerbLogDTO::getName).allMatch(herbName::equals)
        ) {
            throw new IllegalArgumentException("All HerbLogDTOs in herbLogDTOList must have same name from herbDTO.");
        }
        return HerbInfoDTO.builder().herbDTO(herbDTO).herbLogDTOList(herbLogDTOList).build();
    }
}
