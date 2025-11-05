package com.budgee.service.impl.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.budgee.enums.VerificationType;
import com.budgee.event.application.VerificationCodeCreatedEvent;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.model.User;
import com.budgee.model.VerificationCode;
import com.budgee.payload.request.SendVerificationRequest;
import com.budgee.payload.request.VerificationRequest;
import com.budgee.payload.response.TokenResponse;
import com.budgee.repository.VerificationCodeRepository;
import com.budgee.service.JwtService;
import com.budgee.service.UserService;
import com.budgee.service.VerificationCodeService;
import com.budgee.service.lookup.UserLookup;
import com.budgee.util.CodeGenerator;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "VERIFICATION-CODE-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationCodeServiceImpl implements VerificationCodeService {

    // -------------------------------------------------------------------
    // FIELDS PRIVATE
    // -------------------------------------------------------------------
    @NonFinal
    @Value("${spring.application.verification.expiry-minutes:10}")
    long EXPIRY_MINUTES;

    @NonFinal
    @Value("${spring.application.verification.cooldown-seconds:60}")
    long COOLDOWN_SECONDS;

    @NonFinal int CODE_LENGTH = 6;

    long MIN_SECOND_PER_REQUEST = 60;

    Clock clock = Clock.systemDefaultZone();

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    UserLookup userLookup;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    JwtService jwtService;
    UserService userService;

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    VerificationCodeRepository verificationCodeRepository;

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------
    CodeGenerator codeGenerator;

    // -------------------------------------------------------------------
    // PUBLISHER
    // -------------------------------------------------------------------
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void getVerificationCode(SendVerificationRequest request) {
        log.info("[getVerificationCode]={}", request);

        String email = request.email();

        User user = userLookup.getUserByEmail(email);

        sendCode(user, request.type(), email);
    }

    @Override
    @Transactional
    public void sendCode(User user, VerificationType type, String target) {
        log.info("[sendCode] user={} type={} target={}", user.getId(), type, target);

        VerificationCode lastestVerificationCode =
                verificationCodeRepository.findLastestVerificationCodeByUserAndType(user, type);

        ensureVerificationCooldownRespected(lastestVerificationCode);

        verificationCodeRepository.deleteByUserAndType(user, type);

        String code = codeGenerator.generateVerificationToken(CODE_LENGTH);
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(EXPIRY_MINUTES);

        VerificationCode verificationCode =
                VerificationCode.builder()
                        .user(user)
                        .code(code)
                        .target(target)
                        .type(type)
                        .verified(false)
                        .expiresAt(expiresAt)
                        .build();

        verificationCodeRepository.save(verificationCode);

        eventPublisher.publishEvent(new VerificationCodeCreatedEvent(verificationCode.getId()));
    }

    @Override
    public TokenResponse verifyCode(VerificationRequest request) {
        log.info("[verifyCode] target={} type={}", request.email(), request.type());

        String email = request.email();

        User user = userLookup.getUserByEmail(email);

        VerificationCode codeEntity =
                verificationCodeRepository
                        .findTopByUserAndTypeAndCodeOrderByCreatedAtDesc(
                                user, request.type(), request.code())
                        .orElseThrow(() -> new ValidationException(null));

        if (codeEntity.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            throw new ValidationException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (codeEntity.isVerified()) {
            throw new ValidationException(ErrorCode.VERIFICATION_CODE_ALREADY_USED);
        }

        codeEntity.setVerified(true);
        verificationCodeRepository.save(codeEntity);

        switch (request.type()) {
            case VerificationType.REGISTER -> {
                userService.activateUser(user.getId());

                return getTokenResponse(user);
            }
            case VerificationType.PASSWORD_RESET -> {
                //                return tokenService.generateResetToken(user);
            }
            default -> throw new ValidationException(ErrorCode.INVALID_VERIFICATION_TYPE);
        }

        return null;
    }

    @Override
    @Transactional
    public void resendCode(SendVerificationRequest request) {
        log.info("[resendCode]={}", request);

        String email = request.email();
        User user = userLookup.getUserByEmail(email);

        sendCode(user, request.type(), email);
    }

    @Override
    public boolean isVerified(User user, VerificationType type) {
        log.info("[isVerified] userId={} type={}", user.getId(), type);

        Optional<VerificationCode> latest =
                verificationCodeRepository.findLatestByUserAndType(user, type).stream().findFirst();

        return latest.map(VerificationCode::isVerified).orElse(false);
    }

    void ensureVerificationCooldownRespected(VerificationCode verificationCode) {
        log.info("[ensureVerificationCooldownRespected]");

        if (Objects.isNull(verificationCode)) return;

        long seconds =
                Duration.between(
                                verificationCode.getCreatedAt().atZone(clock.getZone()),
                                LocalDateTime.now(clock).atZone(clock.getZone()))
                        .getSeconds();

        if (seconds < MIN_SECOND_PER_REQUEST) {
            log.error(
                    "[ensureVerificationCooldownRespected] you are sending {} per request",
                    seconds);

            throw new ValidationException(ErrorCode.SEND_TOO_FAST);
        }
    }

    TokenResponse getTokenResponse(User user) {
        log.info("[getTokenResponse] userId={}", user.getId());

        return TokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateAccessToken(user))
                .build();
    }
}
