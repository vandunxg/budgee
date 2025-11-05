package com.budgee.service.impl.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.budgee.enums.TokenType;
import com.budgee.enums.VerificationType;
import com.budgee.event.application.UserRegisteredEvent;
import com.budgee.exception.ErrorCode;
import com.budgee.factory.UserFactory;
import com.budgee.model.User;
import com.budgee.payload.request.LoginRequest;
import com.budgee.payload.request.RegisterRequest;
import com.budgee.payload.response.RegisterResponse;
import com.budgee.payload.response.TokenResponse;
import com.budgee.repository.UserRepository;
import com.budgee.service.AuthService;
import com.budgee.service.JwtService;
import com.budgee.service.VerificationCodeService;

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
    VerificationCodeService verificationCodeService;

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------
    Clock clock = Clock.systemDefaultZone();

    // -------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------
    UserFactory userFactory;

    // -------------------------------------------------------------------
    // PUBLISHER
    // -------------------------------------------------------------------
    ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public TokenResponse getAccessToken(LoginRequest request) throws AccessDeniedException {
        log.info("[getAccessToken]");

        final String email = normalizeEmail(request.email());
        log.info("getAccessToken start email_fingerprint={}", fingerprint(email));

        try {
            authenticate(email, request.password());
        } catch (AuthenticationException ex) {
            log.warn(
                    "authentication failed email_fingerprint={}, reason={}",
                    fingerprint(email),
                    ex.getClass().getSimpleName());
            throw new com.budgee.exception.AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = findUserByEmail(email);

        user.ensureIsActiveAccount();

        user.setLastLogin(LocalDateTime.now(clock));
        userRepository.save(user);

        return issueTokens(user);
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

            return issueTokens(user);
        } catch (Exception ex) {
            log.warn(
                    "refresh token invalid fp={}, reason={}",
                    fingerprint(refreshToken),
                    ex.getClass().getSimpleName());
            throw new AccessDeniedException("Invalid or expired token");
        }
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("[register] create user with email {}", request.email());

        final String email = normalizeEmail(request.email());
        checkUserExistsByEmail(email);

        User user = userFactory.createUser(request, email);

        VerificationType REGISTER_VERIFICATION_TYPE = VerificationType.REGISTER;

        userRepository.save(user);
        log.info("createUser success id={}", user.getId());

        verificationCodeService.sendCode(user, REGISTER_VERIFICATION_TYPE, email);

        eventPublisher.publishEvent(new UserRegisteredEvent(email, REGISTER_VERIFICATION_TYPE));

        return RegisterResponse.builder().userId(user.getId()).build();
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void authenticate(String email, String password) {
        try {
            var authRequest = new UsernamePasswordAuthenticationToken(email, password);
            authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException ex) {
            throw new com.budgee.exception.AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    TokenResponse issueTokens(User user) {
        return TokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    void checkUserExistsByEmail(String email) {
        log.info("[checkUserExistsByEmail]: {}", email);
        if (userRepository.existsByEmail(email)) {
            log.error("[checkUserExistsByEmail] email already exists");
            throw new com.budgee.exception.AuthenticationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

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
        log.info("findUserByEmail email_fp={}", fingerprint(email));

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
    }
}
