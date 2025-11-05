package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgee.enums.UserStatus;
import com.budgee.exception.AuthenticationException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.factory.UserFactory;
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
    // FACTORY
    // -------------------------------------------------------------------
    UserFactory userFactory;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public RegisterResponse createUser(RegisterRequest request) {
        log.info("[createUser] create user with email {}", request.email());

        final String email = normalizeEmail(request.email());
        checkUserExistsByEmail(email);

        User user = userFactory.createUser(request, email);

        userRepository.save(user);
        log.info("createUser success id={}", user.getId());

        return RegisterResponse.builder().userId(user.getId()).build();
    }

    @Override
    public void activateUser(UUID userId) {
        log.info("[activateUser] userId={}", userId);

        User user = getUserById(userId);

        user.setStatus(UserStatus.ACTIVE);

        log.info("[activateUser] update active user");
        userRepository.save(user);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    User getUserById(UUID userId) {
        log.info("[getUserById]={}", userId);

        return userRepository
                .findById(userId)
                .orElseThrow(() -> new AuthenticationException(ErrorCode.USER_NOT_FOUND));
    }

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
