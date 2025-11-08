package com.budgee.service;

import java.time.Instant;

public interface TokenBlackListService {

    void blackListToken(String refreshToken, Instant expiration);

    boolean isBlackListed(String refreshToken);
}
