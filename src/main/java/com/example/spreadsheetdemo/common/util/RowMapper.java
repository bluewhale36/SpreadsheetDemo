package com.example.spreadsheetdemo.common.util;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface RowMapper<ENTITY extends SheetsEntity> {

    ENTITY toEntity(List<Object> row, Integer rowNum);

    List<List<Object>> toRow(ENTITY entity);

    default LocalDate parseDate(String dateStr) {

        DateTimeFormatter[] CANDIDATES = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy.M.d"),
                DateTimeFormatter.ofPattern("yyyy. M. d"),
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyyMd")
        };

        for (DateTimeFormatter f : CANDIDATES) {
            try {
                return LocalDate.parse(dateStr, f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    default LocalDateTime parseDateTime(String datetimeStr) {

        DateTimeFormatter[] CANDIDATES = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy.M.d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy. M. d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"),
                DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"),
                DateTimeFormatter.ofPattern("yyyyMd H:m:s")
        };

        for (DateTimeFormatter f : CANDIDATES) {
            try {
                return LocalDateTime.parse(datetimeStr, f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    default Long parseLong(String longStr) {
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
