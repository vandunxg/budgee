package com.budgee.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.exception.ValidationException;
import com.budgee.model.Group;
import com.budgee.model.User;
import com.budgee.repository.GroupMemberRepository;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-TRANSACTION-VALIDATOR")
public class GroupTransactionValidator {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupMemberRepository groupMemberRepository;
    SecurityHelper securityHelper;

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

    public void validateAuthenticatedUserIsGroupMember(Group group) {
        log.info("[validateAuthenticatedUserIsGroupMember]");

        User user = securityHelper.getAuthenticatedUser();
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new ValidationException(ErrorCode.USER_NOT_IN_GROUP);
        }
    }

    public void validateMemberBelongsToGroup(Group group, UUID memberId) {
        log.info("validateMemberBelongsToGroup");

        if (groupMemberRepository.findByGroupAndId(group, memberId) == null) {
            throw new NotFoundException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
        }
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------
}
