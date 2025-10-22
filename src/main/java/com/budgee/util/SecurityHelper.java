package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.budgee.model.OwnerEntity;
import com.budgee.model.User;
import com.budgee.service.UserService;

@Component
@Slf4j(topic = "SECURITY_HELPER")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityHelper {

    UserService userService;

    public <T extends OwnerEntity> void checkIsOwner(T entity) {
        log.info("[checkIsOwner]");

        User authenticatedUser = userService.getCurrentUser();

        entity.checkIsOwner(authenticatedUser);
    }
}
