package com.example.spreadsheetdemo.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RollbackFailedException.class)
    public String handleRollbackFailedException(RollbackFailedException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/rollback_failed";
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(GoogleSpreadsheetsAPIException.class)
    public String handleGoogleSpreadsheetsAPIException(GoogleSpreadsheetsAPIException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/google_sheets_api_error";
    }
}
