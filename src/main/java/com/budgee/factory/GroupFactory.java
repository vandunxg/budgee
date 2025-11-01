package com.budgee.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.service.validator.DateValidator;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-FACTORY")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupFactory {

    // -------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------
    GroupMemberFactory groupMemberFactory;

    // -------------------------------------------------------------------
    // VALIDATOR
    // -------------------------------------------------------------------
    DateValidator dateValidator;

    public Group createGroup(GroupRequest request, User authenticatedUser) {
        log.debug("[createGroup]={}", request);

        Group group =
                Group.builder()
                        .name(request.name())
                        .creator(authenticatedUser)
                        .balance(calculateInitialAdvanceBalance(request))
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .isSharing(false)
                        .sharingToken(null)
                        .build();

        group.getMembers().addAll(initialGroupMember(request, group));
        group.setMemberCount(group.getMembers().size());

        return group;
    }

    Set<GroupMember> initialGroupMember(GroupRequest request, Group group) {
        log.debug("[initialGroupMember]");

        return request.groupMembers().stream()
                .map(mem -> groupMemberFactory.createGroupMember(mem, group))
                .collect(Collectors.toSet());
    }

    BigDecimal calculateInitialAdvanceBalance(GroupRequest request) {
        log.debug("[setCalculateInitialBalance]");

        return request.groupMembers().stream()
                .map(GroupMemberRequest::advanceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
