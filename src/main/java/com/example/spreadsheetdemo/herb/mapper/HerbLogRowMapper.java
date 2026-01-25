package com.example.spreadsheetdemo.herb.mapper;

import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;
import com.example.spreadsheetdemo.common.enums.SheetColumnInfo;
import com.example.spreadsheetdemo.common.util.RowMapper;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import com.example.spreadsheetdemo.herb.enums.HerbLogSheetColumnInfo;
import com.example.spreadsheetdemo.herb.repository.HerbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class HerbLogRowMapper implements RowMapper<HerbLog> {

    private final HerbRepository herbRepository;

    @Override
    public HerbLog toEntity(List<Object> row, Integer rowNum, SheetsQuerySpec<HerbLog> querySpec) {

        if (row == null || row.isEmpty()) return null;

        HerbLog.HerbLogBuilder herbLogBuilder = HerbLog.builder();

        herbLogBuilder.rowNum(rowNum);

        for (SheetColumnInfo columnInfo : querySpec.getTargetColumnList()) {

            if (columnInfo instanceof HerbLogSheetColumnInfo info) {
                int columnIndex = info.getColumnIndex();

                if (columnIndex < row.size()) {
                    info.getFieldSetter().accept(herbLogBuilder, row.get(columnIndex));

                    if (info.equals(HerbLogSheetColumnInfo.NAME)) {
                        String name = (String) info.getParser().apply(row.get(columnIndex));

                        herbLogBuilder.herbSupplier(
                                () -> herbRepository.findAllByName(name).map(herbs -> herbs.get(0)).orElse(null)
                        );
                    }
                }
            }
        }

        return herbLogBuilder.build();
    }

    @Override
    public List<List<Object>> toRow(HerbLog herbLog) {
        if (herbLog == null) return Collections.emptyList();

        List<Object> row = List.of(
                herbLog.getLoggedDateTime().toString(), herbLog.getName(), herbLog.getBeforeAmount(), herbLog.getAfterAmount()
        );
        return Collections.singletonList(row);
    }
}
