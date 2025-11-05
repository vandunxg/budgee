package com.budgee.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.budgee.enums.Currency;
import com.budgee.enums.Role;
import com.budgee.enums.SubscriptionTier;
import com.budgee.enums.UserStatus;
import com.budgee.model.User;
import com.budgee.payload.request.RegisterRequest;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "USER-FACTORY")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserFactory {

    // -------------------------------------------------------------------
    // SECURITY
    // -------------------------------------------------------------------
    PasswordEncoder passwordEncoder;

    public User createUser(RegisterRequest request, String email) {
        log.info("[createUser] request={}", request);

        UserStatus status = UserStatus.INACTIVE;
        Currency currency = Currency.VND;
        Role defaultUserRole = Role.USER;
        SubscriptionTier defaultSubscription = SubscriptionTier.BASIC;

        return User.builder()
                .email(email)
                .role(defaultUserRole)
                .status(status)
                .subscriptionTier(defaultSubscription)
                .currency(currency)
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
    }
}
