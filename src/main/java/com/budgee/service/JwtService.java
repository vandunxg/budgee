package com.budgee.service;

import com.budgee.enums.TokenType;
import com.budgee.model.User;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractEmail(String token, TokenType type);
}
