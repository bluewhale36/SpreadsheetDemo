package com.example.spreadsheetdemo.herb.domain;

import com.example.spreadsheetdemo.common.util.Pagination;
import com.example.spreadsheetdemo.herb.dto.HerbLogViewDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class HerbLogPagination extends Pagination {

    private List<HerbLogViewDTO> herbLogViewDTOList;

    public HerbLogPagination(int pageNum, int totalCount) {
        super(pageNum, totalCount);
    }
}
