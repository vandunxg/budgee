package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.budgee.exception.AuthenticationException;
import com.budgee.exception.ErrorCode;
import com.budgee.model.OwnerEntity;
import com.budgee.model.User;

@Component
@Slf4j(topic = "SECURITY-HELPER")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthContext {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------

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

    public <T extends OwnerEntity> void checkIsOwner(T entity) {
        log.info("[checkIsOwner]");

        User authenticatedUser = this.getAuthenticatedUser();

        entity.checkIsOwner(authenticatedUser);
    }

    public User getAuthenticatedUser() {
        log.info("[getAuthenticatedUser]");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationException(ErrorCode.FORBIDDEN);
        }

        return user;
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

}
