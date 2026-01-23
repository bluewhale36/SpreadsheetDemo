package com.example.spreadsheetdemo.herb.mapper;

import com.example.spreadsheetdemo.common.util.RowMapper;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class HerbRowMapper implements RowMapper<Herb> {
    @Override
    public Herb toEntity(List<Object> row, Integer rowNum) {
        Herb entity = null;

        if (row == null || row.isEmpty()) {
            return entity;
        }

        switch (row.size()) {
            // 아 이렇게 하드코딩 하기 싫다..ㅎ
            case 2:
                // 필수값만 있는 경우
                entity = Herb.of(rowNum, row.get(0).toString(), parseLong(row.get(1).toString()));
                break;
            case 3:
                // 메모가 없는 경우
                entity = Herb.of(rowNum, row.get(0).toString(), parseLong(row.get(1).toString()), parseDate(row.get(2).toString()));
                break;
            case 4:
                // 모든 값이 있는 경우
                entity = Herb.of(rowNum, row.get(0).toString(), parseLong(row.get(1).toString()), parseDate(row.get(2).toString()), row.get(3).toString());
                break;
            default:
        }
        return entity;
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
