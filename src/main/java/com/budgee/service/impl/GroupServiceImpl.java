package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupSharingStatus;
import com.budgee.exception.*;
import com.budgee.mapper.GroupMapper;
import com.budgee.model.*;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;
import com.budgee.payload.response.group.GroupSharingResponse;
import com.budgee.payload.response.group.GroupSharingTokenResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupRepository;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.GroupMemberService;
import com.budgee.service.GroupService;
import com.budgee.service.GroupSharingService;
import com.budgee.service.UserService;
import com.budgee.util.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupServiceImpl implements GroupService {

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------
    Integer SHARING_TOKEN_LENGTH = 5;

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;
    GroupMemberRepository groupMemberRepository;
    GroupTransactionRepository groupTransactionRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    GroupMemberService groupMemberService;
    UserService userService;
    GroupSharingService groupSharingService;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GroupMapper groupMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    DateValidator dateValidator;
    SecurityHelper securityHelper;
    GroupTransactionHelper groupTransactionHelper;
    CodeGenerator codeGenerator;
    GroupValidator groupValidator;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public GroupResponse createGroup(GroupRequest request) {
        log.info("[createGroup]={}", request);

        User authenticatedUser = userService.getCurrentUser();
        Group group = groupMapper.toGroup(request, authenticatedUser);

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());

        setCalculateInitialBalance(request, group);
        setMembersForGroup(request, group);

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

        groupValidator.assertGroupMemberPermission(group);

        return toGroupResponse(group);
    }

    @Override
    public List<GroupResponse> getListGroups() {
        log.info("[getListGroups]");

        List<Group> groups = findAllGroupByAuthenticatedUser();

        return toListGroupResponse(groups);
    }

    @Override
    public GroupSharingTokenResponse getGroupSharingToken(UUID groupId) {
        log.info("[getGroupSharingToken] groupId={}", groupId);

        Group group = getGroupById(groupId);

        if (group.getIsSharing()) {

            return mapToGroupSharingTokenResponse(group, group.getSharingToken());
        }

        String sharingToken = codeGenerator.generateGroupInviteToken(SHARING_TOKEN_LENGTH);
        setGroupSharing(group, sharingToken);

        return mapToGroupSharingTokenResponse(group, sharingToken);
    }

    @Transactional
    @Override
    public GroupSharingResponse joinGroup(UUID groupId, String sharingToken) {
        log.info("[joinGroup] groupId={} sharingToken={}", groupId, sharingToken);

        Group group = getGroupById(groupId);
        User user = securityHelper.getAuthenticatedUser();

        groupValidator.ensureNotAdminJoining(group, user);
        groupValidator.ensureJoinEligibility(user, group);
        groupValidator.ensureGroupIsSharing(group);
        groupValidator.ensureValidToken(group, sharingToken);

        groupSharingService.createGroupSharing(user, group, sharingToken);

        return GroupSharingResponse.builder()
                .groupId(groupId)
                .status(GroupSharingStatus.PENDING)
                .build();
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void setMembersForGroup(GroupRequest request, Group group) {
        log.info("[setMembersForGroup]");

        List<GroupMember> groupMembers = createGroupMembers(request.groupMembers(), group);
        group.setMembers(new HashSet<>(groupMembers));
        group.setMemberCount(groupMembers.size());
    }

    void setCalculateInitialBalance(GroupRequest request, Group group) {
        log.info("[setCalculateInitialBalance]");

        BigDecimal initialBalance =
                request.groupMembers().stream()
                        .map(GroupMemberRequest::advanceAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        group.setBalance(initialBalance);
    }

    void setGroupSharing(Group group, String sharingToken) {
        log.info("[setGroupSharing]");

        group.setIsSharing(Boolean.TRUE);
        group.setSharingToken(sharingToken);

        log.warn("[setGroupSharing] update group to db");
        groupRepository.save(group);
    }

    GroupSharingTokenResponse mapToGroupSharingTokenResponse(Group group, String sharingToken) {
        log.info("[mapToGroupSharingTokenResponse]");

        return GroupSharingTokenResponse.builder()
                .token(sharingToken)
                .groupId(group.getId())
                .build();
    }

    List<GroupResponse> toListGroupResponse(List<Group> groups) {
        log.info("[toListGroupResponse]");

        return groups.stream()
                .map(
                        group -> {
                            List<GroupTransaction> transactions =
                                    groupTransactionRepository.findAllByGroup(group);
                            BigDecimal totalSponsorship =
                                    groupTransactionHelper.calculateTotalSponsorship(transactions);
                            BigDecimal totalIncome =
                                    groupTransactionHelper.calculateTotalIncome(transactions);
                            BigDecimal totalExpense =
                                    groupTransactionHelper.calculateTotalExpense(transactions);

                            BigDecimal totalIncomeAndExpense = totalSponsorship.add(totalIncome);

                            return groupMapper.toGroupResponse(
                                    group, totalIncomeAndExpense, totalExpense);
                        })
                .toList();
    }

    List<Group> findAllGroupByAuthenticatedUser() {
        log.info("[findAllGroupByAuthenticatedUser]");

        User authenticatedUser = securityHelper.getAuthenticatedUser();

        List<GroupMember> members = groupMemberRepository.findAllByUser(authenticatedUser);

        return members.stream().map(GroupMember::getGroup).toList();
    }

    GroupResponse toGroupResponse(Group group) {
        log.info("[toGroupResponse]");

        List<GroupTransaction> transactions = groupTransactionRepository.findAllByGroup(group);
        BigDecimal totalSponsorship =
                groupTransactionHelper.calculateTotalSponsorship(transactions);
        GroupResponse response = groupMapper.toGroupResponse(group, totalSponsorship);

        response.setMembers(
                group.getMembers().stream()
                        .map(x -> groupMemberService.toGroupMemberResponse(x, group))
                        .toList());

        return response;
    }

    List<GroupMember> createGroupMembers(List<GroupMemberRequest> request, Group group) {
        log.info("[createGroupMember]={}", request);

        groupValidator.checkJustOnlyOneCreator(request);

        return request.stream().map(x -> groupMemberService.createGroupMember(x, group)).toList();
    }
}
