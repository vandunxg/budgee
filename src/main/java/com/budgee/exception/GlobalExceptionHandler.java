package com.budgee.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.budgee.payload.response.ErrorResponse;
import com.budgee.util.ResponseUtil;

@RestControllerAdvice
@Slf4j(topic = "GLOBAL-HANDLER-EXCEPTION")
public class GlobalExceptionHandler {

    @ExceptionHandler(BudgeeException.class)
    public ResponseEntity<ErrorResponse> handleBudgeeException(BudgeeException ex) {
        log.warn("[BudgeeException] {}", ex.getMessage());
        return ResponseUtil.error(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

        log.debug("[Validation Error] {}", errors);
        return ResponseUtil.error(ErrorCode.VALIDATION_FAILED, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {
        Map<String, String> violations = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> violations.put(v.getPropertyPath().toString(), v.getMessage()));

        log.debug("[ConstraintViolation] {}", violations);
        return ResponseUtil.error(ErrorCode.VALIDATION_FAILED, "Constraint violation", violations);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("[Unhandled Exception]", ex);
        return ResponseUtil.error(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
