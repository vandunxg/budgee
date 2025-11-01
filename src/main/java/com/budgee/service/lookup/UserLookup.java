package com.budgee.service.lookup;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.User;
import com.budgee.repository.UserRepository;

@Component
@Slf4j(topic = "USER-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    UserRepository userRepository;

    public User getUserById(UUID userId) {
        log.debug("[getUserById]={}", userId);

        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
