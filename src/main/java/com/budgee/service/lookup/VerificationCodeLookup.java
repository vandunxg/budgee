package com.budgee.service.lookup;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.budgee.enums.VerificationType;
import com.budgee.model.User;
import com.budgee.model.VerificationCode;
import com.budgee.repository.VerificationCodeRepository;

@Component
@Slf4j(topic = "VERIFICATION-CODE-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationCodeLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    VerificationCodeRepository verificationCodeRepository;

    public VerificationCode getLastestVerificationCodeByUserAndVerificationType(
            User user, VerificationType type) {
        log.info("[getLastestVerificationCodeByUserAndVerificationType]");

        return verificationCodeRepository.findLastestVerificationCodeByUserAndType(user, type);
    }
}
