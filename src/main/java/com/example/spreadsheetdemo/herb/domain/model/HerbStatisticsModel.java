package com.example.spreadsheetdemo.herb.domain.model;

import com.example.spreadsheetdemo.herb.dto.HerbDTO;
import com.example.spreadsheetdemo.herb.dto.HerbLogDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@ToString
public class HerbStatisticsModel {

    private final LocalDate from;
    private final LocalDate to;

    Map<HerbDTO, HerbStatisticsSpecModel> specMap;

    public static HerbStatisticsModel of(LocalDate from, LocalDate to, Map<HerbDTO, List<HerbLogDTO>> herbListMap) {
        HerbStatisticsModel.HerbStatisticsModelBuilder builder = HerbStatisticsModel.builder();

        builder.from(from).to(to);

        Map<HerbDTO, HerbStatisticsSpecModel> specMap = new HashMap<>(herbListMap.size());

        for (Map.Entry<HerbDTO, List<HerbLogDTO>> entry : herbListMap.entrySet()) {
            HerbDTO key = entry.getKey();
            List<HerbLogDTO> value = entry.getValue();

            specMap.put(key, HerbStatisticsSpecModel.of(value));
        }

        builder.specMap(specMap);

        return builder.build();
    }

    @Getter
    @Builder
    @ToString
    static class HerbStatisticsSpecModel {

        @ToString.Exclude
        private final List<HerbLogDTO> herbLogDTOList;

        private final long totalStored;
        private final long totalDelivered;

        private static HerbStatisticsSpecModel of(List<HerbLogDTO> herbLogDTOList) {
            long totalStored = 0L, totalDelivered = 0L;
            for (HerbLogDTO logDTO : herbLogDTOList) {
                long
                        before = logDTO.getBeforeAmount() == null ? 0L : logDTO.getBeforeAmount(),
                        after = logDTO.getAfterAmount() == null ? 0L : logDTO.getAfterAmount();
                if (logDTO.isAmountIncreased()) {
                    totalStored += after - before;
                } else {
                    totalDelivered += before - after;
                }
            }
            return HerbStatisticsSpecModel.builder()
                    .herbLogDTOList(herbLogDTOList)
                    .totalStored(totalStored)
                    .totalDelivered(totalDelivered)
                    .build();
        }
    }
}
