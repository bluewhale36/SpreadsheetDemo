package com.example.spreadsheetdemo.common.exception;

public class OptimisticLockingException extends RuntimeException {
    public OptimisticLockingException(String message) {
        super(message);
    }
}
