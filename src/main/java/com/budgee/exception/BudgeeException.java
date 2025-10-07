package com.budgee.exception;

import lombok.Getter;

@Getter
public abstract class BudgeeException extends RuntimeException {
    private final ErrorCode errorCode;

    public BudgeeException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BudgeeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BudgeeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}