package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.budgee.exception.AuthenticationException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.GroupMapper;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupRepository;
import com.budgee.service.GroupMemberService;
import com.budgee.service.GroupService;
import com.budgee.service.UserService;
import com.budgee.util.DateValidator;
import com.budgee.util.SecurityHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupServiceImpl implements GroupService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;
    GroupMemberRepository groupMemberRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    GroupMemberService groupMemberService;
    UserService userService;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GroupMapper groupMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    DateValidator dateValidator;
    SecurityHelper securityHelper;

    @Override
    public GroupResponse createGroup(GroupRequest request) {
        log.info("[createGroup]={}", request);

        User authenticatedUser = userService.getCurrentUser();
        Group group = groupMapper.toGroup(request);

        List<GroupMember> groupMembers = createGroupMembers(request.groupMembers(), group);

        BigDecimal initialBalance =
                request.groupMembers().stream()
                        .map(GroupMemberRequest::advanceAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());

        group.setCreator(authenticatedUser);
        group.setBalance(initialBalance);
        group.setMembers(new HashSet<>(groupMembers));
        group.setMemberCount(groupMembers.size());

        log.warn("[createGroup] save group to db");
        groupRepository.save(group);

        return toGroupResponse(group);
    }

    @Override
    public Group getGroupById(UUID groupId) {
        log.info("[getGroupById]={}", groupId);

        return groupRepository
                .findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Override
    public GroupResponse getGroup(UUID id) {
        log.info("[getGroup]={}", id);

        Group group = getGroupById(id);

        assertGroupMemberPermission(group);

        return toGroupResponse(group);
    }

    // PRIVATE FUNCTION

    void assertGroupMemberPermission(Group group) {
        log.info("[assertGroupMemberPermission]");

        User authenticatedUser = securityHelper.getAuthenticatedUser();
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, authenticatedUser);

        if (Objects.isNull(member)) {
            throw new AuthenticationException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
        }
    }

    GroupResponse toGroupResponse(Group group) {
        log.info("[toGroupResponse]");

        GroupResponse response = groupMapper.toGroupResponse(group);
        response.setMembers(
                group.getMembers().stream()
                        .map(groupMemberService::toGroupMemberResponse)
                        .toList());

        return response;
    }

    List<GroupMember> createGroupMembers(List<GroupMemberRequest> request, Group group) {
        log.info("[createGroupMember]={}", request);

        return request.stream().map(x -> groupMemberService.createGroupMember(x, group)).toList();
    }
}
