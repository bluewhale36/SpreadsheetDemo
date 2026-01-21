package com.example.spreadsheetdemo.common.util;

import lombok.Getter;

@Getter
public abstract class Pagination {

    // 현재 페이지 번호
    private final int pageNum;
    // 총 데이터 개수
    private final int totalCount;

    // 페이지 당 데이터 개수
    private int pageSize = 100;
    // 최소 열번호
    private int minRowNum = 2;

    // 시작 열번호
    private final int startRowNum;
    // 종료 열번호
    private final int endRowNum;

    // 다음 페이지 존재 여부
    private final boolean hasNextPage;

    protected Pagination(int pageNum, int totalCount) {
        this.pageNum = pageNum;
        this.totalCount = totalCount;

        this.endRowNum = Math.max(minRowNum, totalCount - ( (pageNum -1) * pageSize) );
        this.startRowNum = Math.max(minRowNum, endRowNum - pageSize + 1);

        this.hasNextPage = startRowNum > minRowNum;
    }

    protected void setPageSize(int pageSize) {
        this.pageSize = Math.max(pageSize, 1);
    }

    protected void setMinRowNum(int minRowNum) {
        this.minRowNum = Math.max(minRowNum, 1);
    }
}
