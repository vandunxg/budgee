package com.budgee.listener.verification_codes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.budgee.event.application.VerificationCodeCreatedEvent;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.User;
import com.budgee.model.VerificationCode;
import com.budgee.repository.VerificationCodeRepository;
import com.budgee.service.EmailService;
import com.budgee.service.lookup.UserLookup;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "VERIFICATION-CODE-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationCodeEventHandler {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    VerificationCodeRepository verificationCodeRepository;

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    EmailService emailService;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    UserLookup userLookup;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationCodeCreated(VerificationCodeCreatedEvent event) {
        UUID verificationCodeId = event.verificationCodeId();
        log.info("[onVerificationCodeCreated] verificationCodeId={}", verificationCodeId);

        VerificationCode verificationCode =
                verificationCodeRepository
                        .findById(verificationCodeId)
                        .orElseThrow(
                                () -> new NotFoundException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        String email = verificationCode.getTarget();
        User user = userLookup.getUserByEmail(email);
        String fullName = user.getFullName();
        String code = verificationCode.getCode();

        emailService.sendRegisterEmail(email, fullName, null, code);
    }
}
