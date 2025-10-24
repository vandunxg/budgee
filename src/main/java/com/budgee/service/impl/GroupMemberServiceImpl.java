package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.mapper.GroupMemberMapper;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.response.group.GroupMemberResponse;
import com.budgee.service.GroupMemberService;
import com.budgee.util.SecurityHelper;
import com.budgee.util.UserHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-MEMBER-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberServiceImpl implements GroupMemberService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GroupMemberMapper groupMemberMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    SecurityHelper securityHelper;
    UserHelper userHelper;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public GroupMember createGroupMember(GroupMemberRequest request, Group group) {
        log.info("[createGroupMember]={}", request);

        User authenticatedUser = securityHelper.getAuthenticatedUser();

        UUID userId = request.userId();
        boolean isCreator = false;
        User memberUser = null;

        if (!Objects.isNull(userId)) {
            checkUserIdAndAuthenticatedUserId(userId, authenticatedUser);

            memberUser = userHelper.getUserById(request.userId());
        }

        if (!Objects.isNull(memberUser)) {
            isCreator = authenticatedUser.getId().equals(memberUser.getId());
        }

        final GroupRole ROLE_FOR_MEMBER = isCreator ? GroupRole.ADMIN : GroupRole.MEMBER;

        GroupMember member = this.createMember(request, group);
        member.setRole(ROLE_FOR_MEMBER);
        member.setUser(Objects.isNull(memberUser) ? null : memberUser);

        return member;
    }

    @Override
    public GroupMemberResponse toGroupMemberResponse(GroupMember member) {
        log.info("[toGroupMemberResponse]");

        GroupMemberResponse response = groupMemberMapper.toGroupMemberResponse(member);

        /* todo: calculate `totalSponsorship` and `totalAdvanceAmount`, check member is creator
        group */

        return response;
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void checkUserIdAndAuthenticatedUserId(UUID creatorId, User authenticatedUser) {
        log.info(
                "[checkUserIdAndAuthenticatedUserId] creatorId={} from request authenticatedUser={}",
                creatorId,
                authenticatedUser);

        if (!Objects.equals(creatorId, authenticatedUser.getId())) {
            log.error("[checkUserIdAndAuthenticatedUserId] creatorId not equal authenticated user");

            throw new ValidationException(ErrorCode.CREATOR_ID_NOT_AUTHENTICATED_USER);
        }
    }

    GroupMember createMember(GroupMemberRequest request, Group group) {
        log.info("[createMember]={}", request);

        return groupMemberMapper.toGroupMember(request, group);
    }
}
