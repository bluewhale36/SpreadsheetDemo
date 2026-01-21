package com.example.spreadsheetdemo.common.exception;

/**
 * Google Spreadsheet API 쓰기 작업 도중 발생할 수 있는 실패 상황에 대한 트랜잭션 롤백 처리 실패 예외 클래스.<br/>
 * Google Spreadsheet API 와 통신을 지원하는 별도의 인터페이스가 없으므로, 트랜잭션 롤백 처리도 수동으로 구현해야 함.<br/>
 * 이 과정에서 롤백 작업이 실패할 경우 해당 예외가 발생함. 이 경우 <b>연동된 Google Spreadsheet 의 데이터 무결성이 손상</b>될 수 있으므로, <b>관리자에게 즉시 알림이 필요함.</b>
 */
public class RollbackFailedException extends RuntimeException {
    public RollbackFailedException(String message) {
        super(message);
    }
    public RollbackFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
