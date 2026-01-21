package com.example.spreadsheetdemo.common.exception;

/**
 * Google Spreadsheet API 관련 예외 처리용 런타임 예외 클래스.
 */
public class GoogleSpreadsheetsAPIException extends RuntimeException {
    public GoogleSpreadsheetsAPIException(String message) {
        super(message);
    }
    public GoogleSpreadsheetsAPIException(String message, Throwable cause) {
      super(message, cause);
    }
}
