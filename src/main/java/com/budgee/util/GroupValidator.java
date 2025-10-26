package com.budgee.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.budgee.enums.GroupSharingStatus;
import com.budgee.exception.BusinessException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.model.Group;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;
import com.budgee.service.GroupSharingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupValidator {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    GroupSharingService groupSharingService;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    public void ensureNotAdminJoining(Group group, User user) {
        if (Objects.equals(group.getCreator().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.GROUP_ADMIN_CANT_JOIN);
        }
    }

    public void ensureJoinEligibility(User user, Group group) {
        GroupSharing gs = groupSharingService.getGroupSharingByUserAndGroup(user, group);
        if (gs == null) return;
        validateStatus(gs.getStatus());
    }

    public void ensureGroupIsSharing(Group group) {
        if (!Boolean.TRUE.equals(group.getIsSharing())) {
            throw new BusinessException(ErrorCode.GROUP_NOT_SHARING);
        }
    }

    public void ensureValidToken(Group group, String token) {
        if (!Objects.equals(group.getSharingToken(), token)) {
            throw new ValidationException(ErrorCode.SHARING_TOKEN_INVALID);
        }
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    private void validateStatus(GroupSharingStatus status) {
        switch (status) {
            case PENDING -> throw new BusinessException(ErrorCode.JOIN_REQUEST_IS_PENDING);
            case ACCEPTED -> throw new BusinessException(ErrorCode.USER_IN_GROUP);
            case REVOKED, EXPIRED -> log.debug("User can rejoin");
            default -> {}
        }
    }
}
