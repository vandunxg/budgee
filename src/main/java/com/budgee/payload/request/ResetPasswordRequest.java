package com.budgee.payload.request;

public record ResetPasswordRequest(String email, String token, String newPassword) {}
