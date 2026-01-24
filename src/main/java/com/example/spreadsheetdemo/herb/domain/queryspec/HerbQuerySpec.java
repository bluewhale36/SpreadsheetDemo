package com.example.spreadsheetdemo.herb.domain.queryspec;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;

public class HerbQuerySpec extends SheetsQuerySpec {

    public HerbQuerySpec(
            String startColumn, String endColumn, Integer startRowNum, Integer endRowNum
    ) {
        super(SheetsInfo.HERB, startColumn, endColumn, startRowNum, endRowNum);
    }

    public HerbQuerySpec() {
        super(SheetsInfo.HERB);
    }
}
