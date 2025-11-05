package com.budgee.service;

import com.budgee.enums.VerificationType;
import com.budgee.model.User;
import com.budgee.payload.request.SendVerificationRequest;
import com.budgee.payload.request.VerificationRequest;
import com.budgee.payload.response.TokenResponse;

public interface VerificationCodeService {

    void getVerificationCode(SendVerificationRequest request);

    void sendCode(User user, VerificationType type, String target);

    TokenResponse verifyCode(VerificationRequest request);

    void resendCode(SendVerificationRequest request);

    boolean isVerified(User user, VerificationType type);
}
