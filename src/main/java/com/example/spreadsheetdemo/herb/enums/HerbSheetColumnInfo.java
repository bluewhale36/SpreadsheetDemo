package com.example.spreadsheetdemo.herb.enums;

import com.example.spreadsheetdemo.common.enums.SheetColumnInfo;
import com.example.spreadsheetdemo.common.util.ColumnUtils;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public enum HerbSheetColumnInfo implements SheetColumnInfo {

    NAME(
            "name", String.class, "A",
            obj -> (obj == null) ? null : obj.toString(),
            (builder, value) -> builder.name(value.toString())
    ),
    AMOUNT(
            "amount", Long.class, "B",
            obj -> (obj == null) ? null : ColumnUtils.parseLong(obj.toString()),
            (builder, value) -> builder.amount(ColumnUtils.parseLong(value.toString()))
    ),
    LAST_STORED_DATE(
            "last_stored_date", LocalDate.class, "C",
            obj -> (obj == null) ? null : ColumnUtils.parseDate(obj.toString()),
            (builder, value) -> builder.lastStoredDate(ColumnUtils.parseDate(value.toString()))
    ),
    MEMO(
            "memo", String.class, "D",
            obj -> (obj == null) ? null : obj.toString(),
            (builder, value) -> builder.memo(value.toString())
    );

    private final String columnNameInSheet;
    private final Class<?> dataType;
    private final String columnInSheet;
    private final Function<Object, ?> parser;
    private final BiConsumer<Herb.HerbBuilder, Object> fieldSetter;
}
