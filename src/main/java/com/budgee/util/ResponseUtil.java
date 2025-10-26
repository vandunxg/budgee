package com.budgee.util;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.budgee.exception.ErrorCode;
import com.budgee.payload.response.ApiResponse;
import com.budgee.payload.response.ErrorResponse;

public final class ResponseUtil {

    private ResponseUtil() {}

    private static String getPath() {
        var attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getRequest().getRequestURI() : "unknown";
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message, T payload, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(
                        ApiResponse.<T>builder()
                                .message(message)
                                .payload(payload)
                                .path(getPath())
                                .build());
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T payload) {
        return success(message, payload, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T payload) {
        return success(null, payload, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T payload) {
        return success(MessageConstants.CREATE_SUCCESS, payload, HttpStatus.CREATED);
    }

    public static ResponseEntity<ApiResponse<Void>> deleted() {
        return success(MessageConstants.DELETE_SUCCESS, null, HttpStatus.NO_CONTENT);
    }

    public static ResponseEntity<ErrorResponse> error(
            ErrorCode code, String message, Map<String, String> errors) {
        ErrorResponse body =
                ErrorResponse.builder()
                        .success(false)
                        .timestamp(Instant.now())
                        .code(code.getCode())
                        .status(code.getHttpStatus().value())
                        .error(code.getHttpStatus().getReasonPhrase())
                        .message(message != null ? message : code.getDefaultMessage())
                        .path(getPath())
                        .errors(errors)
                        .build();
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }

    public static ResponseEntity<ErrorResponse> error(ErrorCode code, String message) {
        return error(code, message, null);
    }

    public static ResponseEntity<ErrorResponse> error(ErrorCode code) {
        return error(code, null, null);
    }
}
