package com.budgee.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.budgee.enums.GroupRole;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.util.AuthContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-MEMBER-FACTORY")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberFactory {

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    AuthContext authContext;

    public GroupMember createGroupMember(GroupMemberRequest request, Group group) {
        log.debug("[createGroupMember]");

        User authenticatedUser = authContext.getAuthenticatedUser();
        Boolean isCreator = request.isCreator();

        final GroupRole ROLE_FOR_MEMBER = isCreator ? GroupRole.ADMIN : GroupRole.MEMBER;

        return GroupMember.builder()
                .memberName(request.memberName())
                .group(group)
                .role(ROLE_FOR_MEMBER)
                .joinedAt(LocalDateTime.now())
                .balanceOwed(BigDecimal.ZERO)
                .advanceAmount(request.advanceAmount())
                .user(isCreator ? authenticatedUser : null)
                .build();
    }
}
