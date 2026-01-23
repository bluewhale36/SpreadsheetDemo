package com.example.spreadsheetdemo.herb.domain;


import com.example.spreadsheetdemo.herb.dto.HerbLogViewDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder(access = lombok.AccessLevel.PRIVATE)
@ToString
public class HerbLogPagination {

    private List<HerbLogViewDTO> data;

    private final int defaultPageSize = 100;

    private int startRowNum;
    private int endRowNum;

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean hasNextPage;

    public static HerbLogPagination of(List<HerbLogViewDTO> data, int startRowNum, int endRowNum, LocalDate startDate, LocalDate endDate) {
        return HerbLogPagination.builder()
                .data(data)
                .startRowNum(startRowNum)
                .endRowNum(endRowNum)
                .startDate(startDate)
                .endDate(endDate)
                .hasNextPage(startRowNum > 2)
                .build();
    }

}
