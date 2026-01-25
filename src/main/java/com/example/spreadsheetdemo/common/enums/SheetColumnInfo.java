package com.example.spreadsheetdemo.common.enums;

import com.example.spreadsheetdemo.common.util.ColumnUtils;

import java.util.function.BiConsumer;
import java.util.function.Function;


public interface SheetColumnInfo {
    String getColumnNameInSheet(); // "name"
    String getColumnInSheet();     // "A"
    Class<?> getDataType();        // String.class
    Function<Object, ?> getParser();
    BiConsumer<?, Object> getFieldSetter();

    // [추가 제안] 컬럼 문자를 0-based 인덱스로 변환하는 유틸리티 메서드 활용
    default int getColumnIndex() {
        return ColumnUtils.toIndex(getColumnInSheet());
    }
}
