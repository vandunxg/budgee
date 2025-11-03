package com.budgee.service;

public interface EmailService {

    void sendRegisterEmail(String toEmail, String fullName, String verificationLink);
}
