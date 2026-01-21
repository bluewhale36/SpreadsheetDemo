package com.example.spreadsheetdemo.common.exception;

/**
 * 낙관적 락(Optimistic Locking) 실패 시 발생하는 예외 클래스.
 */
public class OptimisticLockingException extends RuntimeException {
    public OptimisticLockingException(String message) {
        super(message);
    }
}
