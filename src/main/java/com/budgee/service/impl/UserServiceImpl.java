package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import com.budgee.service.UserService;

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

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    UserMapper userMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public RegisterResponse createUser(RegisterRequest request) {
        log.debug("[createUser] create user with email {}", request.email());

        //        comparePasswordAndConfirmPassword(request.password(), request.confirmPassword());

        final String email = normalizeEmail(request.email());
        checkUserExistsByEmail(email);
        UserStatus status = UserStatus.ACTIVE;
        Currency currency = Currency.VND;
        Role defaultUserRole = Role.USER;
        SubscriptionTier defaultSubscription = SubscriptionTier.BASIC;

        User user =
                userMapper.toUser(request, status, currency, defaultUserRole, defaultSubscription);

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        userRepository.save(user);
        log.debug("createUser success id={}", user.getId());

        return RegisterResponse.builder().userId(user.getId()).build();
    }

    @Override
    public User getCurrentUser() {
        log.debug("[getCurrentUser]");

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
        log.debug("[comparePasswordAndConfirmPassword]");

        if (!password.equals(confirmPassword)) {
            throw new ValidationException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }
    }

    void checkUserExistsByEmail(String email) {
        log.debug("[checkUserExistsByEmail]: {}", email);
        if (userRepository.existsByEmail(email)) {
            log.error("[checkUserExistsByEmail] email already exists");
            throw new AuthenticationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
