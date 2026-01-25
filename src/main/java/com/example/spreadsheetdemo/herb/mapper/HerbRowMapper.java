package com.example.spreadsheetdemo.herb.mapper;

import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;
import com.example.spreadsheetdemo.common.enums.SheetColumnInfo;
import com.example.spreadsheetdemo.common.util.RowMapper;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.enums.HerbSheetColumnInfo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HerbRowMapper implements RowMapper<Herb> {

    @Override
    public Herb toEntity(List<Object> row, Integer rowNum, SheetsQuerySpec<Herb> querySpec) {

        if (row == null || row.isEmpty()) return null;

        Herb.HerbBuilder herbBuilder = Herb.builder();

        herbBuilder.rowNum(rowNum);

        for (SheetColumnInfo columnInfo : querySpec.getTargetColumnList()) {

            if (columnInfo instanceof HerbSheetColumnInfo info) {
                int columnIndex = info.getColumnIndex();

                if (columnIndex < row.size()) {
                    Object value = row.get(columnIndex);
                    info.getFieldSetter().accept(herbBuilder, value);
                }
            }
        }

        return herbBuilder.build();
    }

    @Override
    public List<List<Object>> toRow(Herb herb) {
        if (herb == null) {
            return Collections.emptyList();
        }

        List<Object> row = List.of(
                herb.getName(), herb.getAmount(), herb.getLastStoredDate().toString(), herb.getMemo()
        );
        return Collections.singletonList(row);
    }
}
