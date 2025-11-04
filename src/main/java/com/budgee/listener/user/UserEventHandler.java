package com.budgee.listener.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.budgee.event.application.UserRegisteredEvent;
import com.budgee.model.User;
import com.budgee.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "USER-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventHandler {

    EmailService emailService;

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        User user = event.user();
        log.info("[handleUserRegistered]= {}", user.getEmail());

        emailService.sendRegisterEmail(
                user.getEmail(),
                user.getFullName(),
                user.getVerificationLink(),
                user.getVerificationToken());
    }
}
