package com.budgee.listener.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.budgee.enums.VerificationType;
import com.budgee.event.application.UserRegisteredEvent;
import com.budgee.model.User;
import com.budgee.model.VerificationCode;
import com.budgee.service.EmailService;
import com.budgee.service.lookup.UserLookup;
import com.budgee.service.lookup.VerificationCodeLookup;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "USER-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventHandler {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    EmailService emailService;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    UserLookup userLookup;
    VerificationCodeLookup verificationCodeLookup;

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------
    @NonFinal
    @Value("${spring.application.url}")
    String BASE_URL;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        String email = event.target();
        VerificationType type = event.type();
        log.info("[onUserRegistered] email={}", email);

        User user = userLookup.getUserByEmail(email);

        VerificationCode verificationCode =
                verificationCodeLookup.getLastestVerificationCodeByUserAndVerificationType(
                        user, type);
        try {
            switch (type) {
                case REGISTER -> emailService.sendRegisterEmail(
                        user.getEmail(),
                        user.getFullName(),
                        buildVerificationLink(verificationCode.getCode()),
                        verificationCode.getCode());
                case PASSWORD_RESET -> emailService.sendForgetPassword();

                default -> log.error("[onUserRegistered] Invalid verification type: {}", type);
            }
        } catch (Exception e) {
            log.info("[onUserRegistered] message={}", e.getMessage());
        }
    }

    String buildVerificationLink(String token) {
        log.info("[buildVerificationLink] token={}", token);

        return BASE_URL.concat("/auth/verify?token=").concat(token);
    }
}
