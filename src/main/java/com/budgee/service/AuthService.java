package com.budgee.service;

import com.budgee.payload.request.LoginRequest;
import com.budgee.payload.response.TokenResponse;

import java.nio.file.AccessDeniedException;

public interface AuthService {

    TokenResponse getAccessToken(LoginRequest request) throws AccessDeniedException;

    TokenResponse getRefreshToken (String refreshToken) throws AccessDeniedException;
}
