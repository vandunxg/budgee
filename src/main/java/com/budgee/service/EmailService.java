package com.budgee.service;

import java.util.UUID;

public interface EmailService {

    void sendRegisterEmail(
            String toEmail, String fullName, String verificationLink, String verificationToken);

    void sendGroupTransactionCreatedEmail(UUID groupId, UUID transactionId);
}
