package com.budgee.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

/** Enum defining error codes for the Budgee application, covering all possible error scenarios. */
@Getter
public enum ErrorCode {
    // User-related errors (1000-1999)
    USER_NOT_FOUND(1000, HttpStatus.NOT_FOUND, "User not found"),
    EMAIL_ALREADY_EXISTS(1001, HttpStatus.BAD_REQUEST, "Email already exists"),
    PHONE_ALREADY_EXISTS(1002, HttpStatus.BAD_REQUEST, "Phone number already exists"),
    INVALID_CREDENTIALS(1003, HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    USER_STATUS_NOT_ALLOWED(
            1004, HttpStatus.UNAUTHORIZED, "User account status does not allow this action"),

    INVALID_EMAIL_FORMAT(1005, HttpStatus.BAD_REQUEST, "Invalid email format"),
    INVALID_PHONE_FORMAT(1006, HttpStatus.BAD_REQUEST, "Invalid phone number format"),
    INVALID_PASSWORD_FORMAT(
            1007,
            HttpStatus.BAD_REQUEST,
            "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"),
    PASSWORDS_DO_NOT_MATCH(1008, HttpStatus.BAD_REQUEST, "Passwords do not match"),
    INVALID_CURRENCY(1009, HttpStatus.BAD_REQUEST, "Invalid currency code"),
    TERMS_NOT_ACCEPTED(1010, HttpStatus.BAD_REQUEST, "Terms and conditions must be accepted"),
    INVALID_BIRTHDAY(1011, HttpStatus.BAD_REQUEST, "Birthday must be in the past"),
    INVALID_PLATFORM(1012, HttpStatus.BAD_REQUEST, "Platform must be WEB, IOS, or ANDROID"),
    INVALID_TOKEN_DEVICE(1013, HttpStatus.BAD_REQUEST, "Device token is invalid or too long"),

    INVALID_TOKEN_TYPE(1100, HttpStatus.UNAUTHORIZED, "Invalid token type"),
    UNAUTHORIZED(1101, HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    INVALID_TOKEN(1102, HttpStatus.UNAUTHORIZED, "Invalid or malformed token"),
    EXPIRED_TOKEN(1103, HttpStatus.UNAUTHORIZED, "Token has expired"),
    UNSUPPORTED_TOKEN(1104, HttpStatus.UNAUTHORIZED, "Unsupported token type"),
    MISSING_TOKEN(1105, HttpStatus.UNAUTHORIZED, "Authorization token is missing"),
    INVALID_REFRESH_TOKEN(1106, HttpStatus.UNAUTHORIZED, "Invalid refresh token"),
    TOKEN_SIGNATURE_INVALID(1107, HttpStatus.UNAUTHORIZED, "Token signature is invalid"),
    ACCESS_TOKEN_REQUIRED(1108, HttpStatus.UNAUTHORIZED, "Access token required"),
    SESSION_EXPIRED(1109, HttpStatus.UNAUTHORIZED, "User session expired, please log in again"),

    FORBIDDEN(1200, HttpStatus.FORBIDDEN, "Access denied"),
    ROLE_NOT_ALLOWED(1201, HttpStatus.FORBIDDEN, "User role not allowed to perform this action"),
    ACCOUNT_LOCKED(1202, HttpStatus.FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(1203, HttpStatus.FORBIDDEN, "User account is disabled"),
    ACCOUNT_NOT_VERIFIED(1204, HttpStatus.FORBIDDEN, "User account is not verified"),
    VERIFICATION_CODE_NOT_MATCH(1205, HttpStatus.FORBIDDEN, "Verification code not match"),
    VERIFICATION_CODE_EXPIRED(1206, HttpStatus.FORBIDDEN, "Verification code expired"),
    VERIFICATION_CODE_ALREADY_USED(1207, HttpStatus.FORBIDDEN, "Verification code already used"),
    INVALID_VERIFICATION_TYPE(1208, HttpStatus.BAD_REQUEST, "Invalid verification type"),
    VERIFICATION_CODE_NOT_FOUND(1209, HttpStatus.FORBIDDEN, "Verification code not found"),
    SEND_TOO_FAST(
            1210,
            HttpStatus.TOO_MANY_REQUESTS,
            "You are sending requests too frequently, please wait before retrying."),
    // Group-related errors (2000-2999)
    GROUP_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "Group not found"),
    GROUP_MEMBER_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "Member not found in group"),
    NOT_GROUP_ADMIN(2002, HttpStatus.FORBIDDEN, "Only group admins can perform this action"),
    CANNOT_DELETE_SELF(
            2003, HttpStatus.BAD_REQUEST, "Admins cannot delete themselves from the group"),
    GROUP_ALREADY_EXISTS(2004, HttpStatus.BAD_REQUEST, "Group name already exists"),
    INVALID_INVITE_ID(2005, HttpStatus.BAD_REQUEST, "Invalid invite ID"),
    INVALID_INVITE_LINK(2006, HttpStatus.BAD_REQUEST, "Invalid invite link"),
    USER_ALREADY_IN_GROUP(2007, HttpStatus.BAD_REQUEST, "User is already a member of the group"),
    GROUP_DATE_INVALID(2008, HttpStatus.BAD_REQUEST, "Group date range is invalid"),
    GROUP_BALANCE_INSUFFICIENT(
            2009, HttpStatus.BAD_REQUEST, "Insufficient group balance for this operation"),
    GROUP_MEMBER_LIMIT_EXCEEDED(2010, HttpStatus.BAD_REQUEST, "Group member limit exceeded"),
    USER_NOT_IN_GROUP(2008, HttpStatus.FORBIDDEN, "User is not a member of this group"),
    CREATOR_ID_NOT_AUTHENTICATED_USER(
            2009, HttpStatus.BAD_REQUEST, "CreatorId is not equal authenticated user"),
    DUPLICATE_CREATOR_ASSIGNMENT(2010, HttpStatus.BAD_REQUEST, "Duplicate creator assignment"),
    SHARING_TOKEN_INVALID(2011, HttpStatus.BAD_REQUEST, "Sharing token invalid"),
    GROUP_NOT_SHARING(2012, HttpStatus.BAD_REQUEST, "Group is not sharing"),
    JOIN_REQUEST_IS_PENDING(2012, HttpStatus.BAD_REQUEST, "Join request is pending"),
    USER_IN_GROUP(2013, HttpStatus.BAD_REQUEST, "User in group"),
    INVALID_JOIN_STATUS(2014, HttpStatus.BAD_REQUEST, "Invalid join status"),
    GROUP_ADMIN_CANT_JOIN(2015, HttpStatus.BAD_REQUEST, "Group admin can't join own group"),
    // Transaction-related errors (3000-3999)
    TRANSACTION_NOT_FOUND(3000, HttpStatus.NOT_FOUND, "Transaction not found"),
    INVALID_TRANSACTION_AMOUNT(
            3001, HttpStatus.BAD_REQUEST, "Transaction amount must be at least 0.01"),
    INVALID_TRANSACTION_TYPE(3002, HttpStatus.BAD_REQUEST, "Invalid transaction type"),
    INVALID_EXPENSE_SOURCE(3003, HttpStatus.BAD_REQUEST, "Invalid expense source"),
    TRANSACTION_DATE_INVALID(
            3004, HttpStatus.BAD_REQUEST, "Transaction date must be in the past or present"),
    TRANSACTION_NOT_ALLOWED(
            3005, HttpStatus.FORBIDDEN, "User is not allowed to perform this transaction"),
    INVALID_CATEGORY(3006, HttpStatus.BAD_REQUEST, "Invalid category for transaction"),
    INVALID_WALLET(3007, HttpStatus.BAD_REQUEST, "Invalid wallet for transaction"),
    INSUFFICIENT_WALLET_BALANCE(3008, HttpStatus.BAD_REQUEST, "Insufficient wallet balance"),
    INVALID_RECURRING_TRANSACTION(
            3009, HttpStatus.BAD_REQUEST, "Invalid recurring transaction configuration"),
    SPONSOR_ADVANCED_ONLY_ONE_FLAG_TRUE(
            3010, HttpStatus.BAD_REQUEST, "Sponsor and advanced only ine flag true"),
    INVALID_GROUP_TRANSACTION_SOURCE(
            3011, HttpStatus.BAD_REQUEST, "Invalid group transaction source"),

    // Category-related errors (4000-4999)
    CATEGORY_NOT_FOUND(4000, HttpStatus.NOT_FOUND, "Category not found"),
    INVALID_CATEGORY_TYPE(4001, HttpStatus.BAD_REQUEST, "Invalid category type"),
    INVALID_CATEGORY_NAME(4002, HttpStatus.BAD_REQUEST, "Category name is invalid or too long"),
    CATEGORY_ALREADY_EXISTS(
            4003, HttpStatus.BAD_REQUEST, "Category name already exists for this user"),
    INVALID_PARENT_CATEGORY(4004, HttpStatus.BAD_REQUEST, "Invalid parent category"),
    CATEGORY_IS_REQUIRED(6002, HttpStatus.BAD_REQUEST, "Category is required"),

    // Debt-related errors (5000-5999)
    DEBT_NOT_FOUND(5000, HttpStatus.NOT_FOUND, "Debt not found"),
    INVALID_DEBT_AMOUNT(5001, HttpStatus.BAD_REQUEST, "Debt amount must be positive"),
    INVALID_DEBT_STATUS(5002, HttpStatus.BAD_REQUEST, "Invalid debt status"),
    DEBT_DUE_DATE_INVALID(5003, HttpStatus.BAD_REQUEST, "Debt due date must be valid"),

    // Wallet-related errors  (6000-6999)
    WALLET_NOT_FOUND(6000, HttpStatus.NOT_FOUND, "Wallet not found"),
    AMOUNT_MUST_BE_POSITIVE(6001, HttpStatus.BAD_REQUEST, "Amount must be positive"),
    WALLET_IS_REQUIRED(6002, HttpStatus.BAD_REQUEST, "Wallet is required"),
    CONCURRENT_BALANCE_UPDATE(6003, HttpStatus.BAD_REQUEST, "Concurrent balance updated fail"),

    // General system errors (7000-7999)
    DATABASE_ERROR(7000, HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed"),
    VALIDATION_FAILED(7001, HttpStatus.BAD_REQUEST, "Validation failed"),
    INTERNAL_SERVER_ERROR(7002, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    INVALID_REQUEST(7003, HttpStatus.BAD_REQUEST, "Invalid request data"),
    OPERATION_NOT_SUPPORTED(7004, HttpStatus.BAD_REQUEST, "Operation not supported"),

    // Goal-related errors (8000-8999)
    GOAL_NOT_FOUND(8000, HttpStatus.NOT_FOUND, "Goal not found"),
    START_DATE_NOT_BEFORE_AFTER_DATE(
            8001, HttpStatus.BAD_REQUEST, "Start date must be before after date"),
    ;

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Finds an ErrorCode by its code.
     *
     * @param code The error code
     * @return ErrorCode or null if not found
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
