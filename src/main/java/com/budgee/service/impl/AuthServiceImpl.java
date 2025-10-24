package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.budgee.enums.TokenType;
import com.budgee.exception.ErrorCode;
import com.budgee.model.User;
import com.budgee.payload.request.LoginRequest;
import com.budgee.payload.response.TokenResponse;
import com.budgee.repository.UserRepository;
import com.budgee.service.AuthService;
import com.budgee.service.JwtService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    UserRepository userRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    JwtService jwtService;
    AuthenticationManager authenticationManager;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------

    Clock clock = Clock.systemDefaultZone();

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public TokenResponse getAccessToken(LoginRequest request) throws AccessDeniedException {
        final String email = normalizeEmail(request.email());
        log.info("getAccessToken start email_fingerprint={}", fingerprint(email));

        try {
            Authentication authRequest =
                    new UsernamePasswordAuthenticationToken(email, request.password());
            Authentication authentication = authenticationManager.authenticate(authRequest);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug(
                    "authenticated={}, authorities={}",
                    authentication.isAuthenticated(),
                    authentication.getAuthorities());
        } catch (AuthenticationException ex) {
            log.warn(
                    "authentication failed email_fingerprint={}, reason={}",
                    fingerprint(email),
                    ex.getClass().getSimpleName());
            throw new AccessDeniedException("Invalid credentials");
        }

        User user = findUserByEmail(email);

        user.setLastLogin(LocalDateTime.now(clock));
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("getAccessToken success user_id={}", user.getId());
        return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) throws AccessDeniedException {
        log.info("getRefreshToken start token_fp={}", fingerprint(refreshToken));

        if (!StringUtils.hasText(refreshToken)) {
            log.warn("refresh token blank");
            throw new com.budgee.exception.AuthenticationException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        try {
            String email = jwtService.extractEmail(refreshToken, TokenType.REFRESH_TOKEN);
            User user = findUserByEmail(email);

            String newAccessToken = jwtService.generateAccessToken(user);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception ex) {
            log.warn(
                    "refresh token invalid fp={}, reason={}",
                    fingerprint(refreshToken),
                    ex.getClass().getSimpleName());
            throw new AccessDeniedException("Invalid or expired token");
        }
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    String fingerprint(String value) {
        if (!StringUtils.hasText(value)) return "empty";
        int len = value.length();
        String tail = value.substring(Math.max(0, len - 4));
        return "***" + tail + "(len=" + len + ")";
    }

    User findUserByEmail(String email) {
        log.debug("findUserByEmail email_fp={}", fingerprint(email));
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
    }
}
