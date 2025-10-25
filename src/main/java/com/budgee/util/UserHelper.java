package com.budgee.util;

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
@RequiredArgsConstructor
@Slf4j(topic = "USER_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserHelper {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    UserRepository userRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    public User getUserById(UUID id) {
        log.info("[getUserById]={}", id);

        return userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

}
