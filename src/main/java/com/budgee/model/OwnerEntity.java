package com.budgee.model;

import java.util.Objects;

import com.budgee.exception.AuthorizationException;
import com.budgee.exception.ErrorCode;

public interface OwnerEntity {

    User getOwner();

    /** Default ownership check for all entities implementing this interface. */
    default void checkIsOwner(User currentUserAuthenticated) {
        if (getOwner() == null || currentUserAuthenticated == null) {
            throw new AuthorizationException(ErrorCode.UNAUTHORIZED);
        }

        if (!Objects.equals(getOwner().getId(), currentUserAuthenticated.getId())) {
            throw new AuthorizationException(ErrorCode.UNAUTHORIZED);
        }
    }
}
