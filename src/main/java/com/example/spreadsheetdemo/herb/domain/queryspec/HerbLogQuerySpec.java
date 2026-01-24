package com.example.spreadsheetdemo.herb.domain.queryspec;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;

public class HerbLogQuerySpec extends SheetsQuerySpec {

    public HerbLogQuerySpec(
            String startColumn, String endColumn, Integer startRowNum, Integer endRowNum
    ) {
        super(SheetsInfo.HERB_LOG, startColumn, endColumn, startRowNum, endRowNum);
    }

    public HerbLogQuerySpec(
            String startColumn, String endColumn
    ) {
        this(startColumn, endColumn, null, null);
    }

    public HerbLogQuerySpec() {
        super(SheetsInfo.HERB_LOG);
    }
}
