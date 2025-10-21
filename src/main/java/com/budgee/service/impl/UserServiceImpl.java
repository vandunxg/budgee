package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import com.budgee.repository.UserRepository;
import com.budgee.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UUID createUser(RegisterRequest request) {
        log.info("[createUser] create user with email {}", request.email());

        comparePasswordAndConfirmPassword(request.password(), request.confirmPassword());

        final String email = normalizeEmail(request.email());
        checkUserExistsByEmail(email);

        User user = UserMapper.INSTANCE.toUser(request);
        user.setFullName(request.fullName());
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCurrency(Currency.VND);

        // Default role always USER
        user.setRole(Role.USER);

        // Default subscription always FREE
        user.setSubscriptionTier(SubscriptionTier.BASIC);

        userRepository.save(user);
        log.info("createUser success id={}", user.getId());
        return user.getId();
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

    // PRIVATE FUNCTION

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

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    /** Avoid logging secrets; return short, non-reversible fingerprints for observability. */
    private static String fingerprint(String secret) {
        if (!StringUtils.hasText(secret)) return "empty";
        int len = secret.length();
        String tail = secret.substring(Math.max(0, len - 4));
        return "***" + tail + "(len=" + len + ")";
    }
}
