package com.budgee.util;

import com.budgee.payload.response.ApiResponse;
import com.budgee.payload.response.ErrorResponse;
import com.budgee.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

public final class ResponseUtil {

    private ResponseUtil() {}

    private static String getPath() {
        var attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getRequest().getRequestURI() : "unknown";
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .path(getPath())
                .build());
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return success(message, data, HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return success(MessageConstants.CREATE_SUCCESS, data, HttpStatus.CREATED);
    }

    public static ResponseEntity<ApiResponse<Void>> deleted() {
        return success(MessageConstants.DELETE_SUCCESS, null, HttpStatus.NO_CONTENT);
    }

    public static ResponseEntity<ErrorResponse> error(ErrorCode code, String message, Map<String, String> errors) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .timestamp(java.time.LocalDateTime.now())
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
