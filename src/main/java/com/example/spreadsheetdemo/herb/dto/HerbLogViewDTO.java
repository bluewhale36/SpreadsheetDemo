package com.example.spreadsheetdemo.herb.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 약재 로그 조회용 DTO
 *
 * @see HerbLogDTO
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class HerbLogViewDTO {

    /**
     * 로그 기록 일자
     */
    private final LocalDate loggedDate;

    /**
     * 약재 이름 별 로그 기록 목록 맵<br/>
     * 키: 약재 이름, 값: 해당 약재 이름의 HerbLogDTO 리스트
     */
    private final Map<String, List<HerbLogDTO>> herbLogListMapByName;

    /**
     * HerbLogDTO 리스트를 HerbLogViewDTO 리스트로 변환.<br/>
     * {@link HerbLogDTO#loggedDateTime} 필드 값을 기준으로 일자 별로 그룹화하고,
     * 각 일자 내에서는 약재 이름 별로 그룹화하여 변환된 리스트를 반환한다.<br/>
     *
     *
     * @param logDTOList 변환할 HerbLogDTO 리스트
     * @return 매개변수로 빈 List 객체 전달 시 비어있는 List 객체를 반환하며, 그렇지 않은 경우 변환된 HerbLogViewDTO 리스트를 반환함.
     */
    public static List<HerbLogViewDTO> from(List<HerbLogDTO> logDTOList) {
        if (logDTOList == null) throw new IllegalArgumentException("logDTOList should not be null while creating HerbLogViewDTO.");
        if (logDTOList.isEmpty()) return List.of();

        List<HerbLogViewDTO> result = new ArrayList<>();

        // 일자 별로 그룹화
        Map<LocalDate, List<HerbLogDTO>> groupedLogsByDate = logDTOList.stream()
                .collect(
                        Collectors.groupingBy(
                                log -> log.getLoggedDateTime().toLocalDate()
                        )
                );

        // 그룹화된 일자 별로 처리
        for (Map.Entry<LocalDate, List<HerbLogDTO>> entry : groupedLogsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<HerbLogDTO> logs = entry.getValue();

            // 기준 일자 내에서 약재 이름 별로 그룹화
            Map<String, List<HerbLogDTO>> groupedLogsByName = logs.stream()
                    .sorted(
                            Comparator.comparing(HerbLogDTO::getLoggedDateTime).reversed()
                    )
                    .collect(
                            Collectors.groupingBy(
                                    HerbLogDTO::getName
                            )
                    );

            result.add(
                    HerbLogViewDTO.builder()
                            .loggedDate(date)
                            .herbLogListMapByName(groupedLogsByName)
                            .build()
            );
        }

        return result.stream().sorted(Comparator.comparing(HerbLogViewDTO::getLoggedDate).reversed()).toList();
    }
}
