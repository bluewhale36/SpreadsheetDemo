package com.example.spreadsheetdemo.herb.enums;

import com.example.spreadsheetdemo.common.enums.SheetColumnInfo;
import com.example.spreadsheetdemo.common.util.ColumnUtils;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public enum HerbLogSheetColumnInfo implements SheetColumnInfo {

    LOGGED_DATE_TIME(
            "logged_datetime", LocalDateTime.class, "A",
            obj -> ColumnUtils.parseDateTime(obj.toString()),
            (builder, value) -> builder.loggedDateTime(ColumnUtils.parseDateTime(value.toString()))
    ),
    NAME(
            "name", String.class, "B",
            Object::toString,
            (builder, value) -> builder.name(value.toString())
    ),
    BEFORE_AMOUNT(
            "before_amount", Long.class, "C",
            obj -> ColumnUtils.parseLong(obj.toString()),
            (builder, value) -> builder.beforeAmount(ColumnUtils.parseLong(value.toString()))
    ),
    AFTER_AMOUNT(
            "after_amount", Long.class, "D",
            obj -> ColumnUtils.parseLong(obj.toString()),
            (builder, value) -> builder.afterAmount(ColumnUtils.parseLong(value.toString()))
    );

    private final String columnNameInSheet;
    private final Class<?> dataType;
    private final String columnInSheet;
    private final Function<Object, ?> parser;
    private final BiConsumer<HerbLog.HerbLogBuilder, Object> fieldSetter;
}
