package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.budgee.enums.GroupSharingStatus;
import com.budgee.exception.AuthenticationException;
import com.budgee.exception.BusinessException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.service.GroupSharingService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-VALIDATOR-HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupValidator {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupMemberRepository groupMemberRepository;

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
    SecurityHelper securityHelper;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    public void checkJustOnlyOneCreator(List<GroupMemberRequest> requests) {
        log.info("[checkJustOnlyOneCreator]");

        AtomicInteger count = new AtomicInteger(0);

        requests.forEach(
                x -> {
                    if (x.isCreator()) {
                        count.getAndIncrement();
                    }
                });

        if (count.get() > 1) {
            throw new ValidationException(ErrorCode.DUPLICATE_CREATOR_ASSIGNMENT);
        }
    }

    public void assertGroupMemberPermission(Group group) {
        log.info("[assertGroupMemberPermission]");

        User authenticatedUser = securityHelper.getAuthenticatedUser();
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, authenticatedUser);

        if (Objects.isNull(member)) {
            log.error("[assertGroupMemberPermission] member is not in group");

            throw new AuthenticationException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
        }
    }

    public void ensureNotAdminJoining(Group group, User user) {
        log.info("[ensureNotAdminJoining]");

        if (Objects.equals(group.getCreator().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.GROUP_ADMIN_CANT_JOIN);
        }
    }

    public void ensureJoinEligibility(User user, Group group) {
        log.info("[ensureJoinEligibility]");

        GroupSharing gs = groupSharingService.getGroupSharingByUserAndGroup(user, group);
        if (gs == null) return;
        validateStatus(gs.getStatus());
    }

    public void ensureGroupIsSharing(Group group) {
        log.info("[ensureGroupIsSharing]");

        if (!Boolean.TRUE.equals(group.getIsSharing())) {
            throw new BusinessException(ErrorCode.GROUP_NOT_SHARING);
        }
    }

    public void ensureValidToken(Group group, String token) {
        log.info("[ensureValidToken]");

        if (!Objects.equals(group.getSharingToken(), token)) {
            throw new ValidationException(ErrorCode.SHARING_TOKEN_INVALID);
        }
    }

    public void ensureUserIsGroupCreator(Group group, User user) {
        log.info("[ensureUserIsGroupCreator]");

        User groupCreator = group.getCreator();

        if (!Objects.equals(groupCreator.getId(), user.getId())) {
            throw new ValidationException(ErrorCode.NOT_GROUP_ADMIN);
        }
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    private void validateStatus(GroupSharingStatus status) {
        log.info("[validateStatus]");

        switch (status) {
            case PENDING -> throw new BusinessException(ErrorCode.JOIN_REQUEST_IS_PENDING);
            case ACCEPTED -> throw new BusinessException(ErrorCode.USER_IN_GROUP);
            case REVOKED, EXPIRED -> log.debug("User can rejoin");
            default -> {}
        }
    }
}
