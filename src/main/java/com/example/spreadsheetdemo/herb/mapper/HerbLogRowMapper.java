package com.example.spreadsheetdemo.herb.mapper;

import com.example.spreadsheetdemo.common.util.RowMapper;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
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
    public HerbLog toEntity(List<Object> row, Integer rowNum) {

        if (row == null || row.isEmpty()) return null;

        LocalDateTime loggedDateTime = parseDateTime(row.get(0).toString());
        String name = row.get(1).toString();
        Long beforeAmount = parseLong(row.get(2).toString()), afterAmount = parseLong(row.get(3).toString());
        Supplier<Herb> herbSupplier = () -> herbRepository.findAllByName(name).map(herbs -> herbs.get(0)).orElse(null);

        return HerbLog.of(rowNum, loggedDateTime, name, beforeAmount, afterAmount, herbSupplier);
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
