package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgee.enums.Currency;
import com.budgee.enums.Role;
import com.budgee.enums.SubscriptionTier;
import com.budgee.enums.UserStatus;
import com.budgee.exception.AuthenticationException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.mapper.UserMapper;
import com.budgee.model.User;
import com.budgee.payload.request.RegisterRequest;
import com.budgee.payload.response.RegisterResponse;
import com.budgee.repository.UserRepository;
import com.budgee.service.EmailService;
import com.budgee.service.UserService;
import com.budgee.util.CodeGenerator;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    UserRepository userRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    CodeGenerator codeGenerator;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    UserMapper userMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------
    @NonFinal String VERIFICATION_LINK = "http://localhost:8080/verify?token=";

    static int VERIFICATION_TOKEN_LENGTH = 5;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public RegisterResponse createUser(RegisterRequest request) {
        log.info("[createUser] create user with email {}", request.email());

        //        comparePasswordAndConfirmPassword(request.password(), request.confirmPassword());

        final String email = normalizeEmail(request.email());
        checkUserExistsByEmail(email);
        UserStatus status = UserStatus.INACTIVE;
        Currency currency = Currency.VND;
        Role defaultUserRole = Role.USER;
        SubscriptionTier defaultSubscription = SubscriptionTier.BASIC;

        User user =
                userMapper.toUser(request, status, currency, defaultUserRole, defaultSubscription);

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        String verificationToken =
                codeGenerator.generateVerificationToken(VERIFICATION_TOKEN_LENGTH);
        user.setVerificationToken(verificationToken);

        userRepository.save(user);
        log.info("createUser success id={}", user.getId());

        String verificationLink = VERIFICATION_LINK.concat(verificationToken);
        emailService.sendRegisterEmail(
                email, user.getFullName(), verificationLink, verificationToken);

        return RegisterResponse.builder().userId(user.getId()).build();
    }

    @Override
    public User getCurrentUser() {
        log.info("[getCurrentUser]");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void comparePasswordAndConfirmPassword(String password, String confirmPassword) {
        log.info("[comparePasswordAndConfirmPassword]");

        if (!password.equals(confirmPassword)) {
            throw new ValidationException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }
    }

    void checkUserExistsByEmail(String email) {
        log.info("[checkUserExistsByEmail]: {}", email);
        if (userRepository.existsByEmail(email)) {
            log.error("[checkUserExistsByEmail] email already exists");
            throw new AuthenticationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
