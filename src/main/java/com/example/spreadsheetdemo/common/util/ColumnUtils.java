package com.example.spreadsheetdemo.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ColumnUtils {

    // A, B, 등 컬럼 알파벳을 0-based index 값으로 변환
    public static int toIndex(String column) {
        int result = 0;
        for (int i = 0; i < column.length(); i++) {
            result *= 26;
            result += column.charAt(i) - 'A' + 1;
        }
        return result - 1;
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

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

    public static LocalDateTime parseDateTime(String datetimeStr) {
        if (datetimeStr == null || datetimeStr.isBlank()) return null;

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

    public static Long parseLong(String longStr) {
        if (longStr == null || longStr.isBlank()) return null;

        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
