package com.budgee.service;

import java.nio.file.AccessDeniedException;

import com.budgee.payload.request.LoginRequest;
import com.budgee.payload.response.TokenResponse;

public interface AuthService {

    TokenResponse getAccessToken(LoginRequest request) throws AccessDeniedException;

    TokenResponse getRefreshToken(String refreshToken) throws AccessDeniedException;
}
