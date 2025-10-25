package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.enums.GroupSharingStatus;
import com.budgee.exception.*;
import com.budgee.mapper.GroupMapper;
import com.budgee.model.*;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;
import com.budgee.payload.response.group.GroupSharingTokenResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupRepository;
import com.budgee.repository.GroupSharingRepository;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.GroupMemberService;
import com.budgee.service.GroupService;
import com.budgee.service.UserService;
import com.budgee.util.CodeGenerator;
import com.budgee.util.DateValidator;
import com.budgee.util.GroupTransactionHelper;
import com.budgee.util.SecurityHelper;

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
    GroupSharingRepository groupSharingRepository;

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
    GroupTransactionHelper groupTransactionHelper;
    CodeGenerator codeGenerator;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

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

    @Override
    public Void joinGroup(UUID groupId, String sharingToken) {
        log.info("[joinGroup] groupId={} sharingToken={}", groupId, sharingToken);

        Group group = getGroupById(groupId);
        User authenticatedUser = securityHelper.getAuthenticatedUser();

        checkGroupIsSharing(group);
        checkValidSharingToken(group, sharingToken);

        createGroupSharing(authenticatedUser, group, sharingToken);

        return null;
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void createGroupSharing(User user, Group group, String sharingToken) {
        log.info("[createGroupSharing]");

        final GroupRole role = GroupRole.MEMBER;
        final GroupSharingStatus status = GroupSharingStatus.PENDING;

        GroupSharing groupSharing =
                GroupSharing.builder()
                        .role(role)
                        .sharedUser(user)
                        .group(group)
                        .status(status)
                        .sharingToken(sharingToken)
                        .joinedAt(LocalDateTime.now())
                        .build();

        log.warn("[createGroupSharing] save group sharing to db");
        groupSharingRepository.save(groupSharing);
    }

    void checkGroupIsSharing(Group group) {
        log.info("[checkGroupIsSharing]");

        if (!group.getIsSharing()) {
            throw new BusinessException(ErrorCode.GROUP_NOT_SHARING);
        }
    }

    void checkValidSharingToken(Group group, String sharingToken) {
        log.info("[checkValidSharingToken]");

        String tokenOfGroup = group.getSharingToken();

        if (!Objects.equals(tokenOfGroup, sharingToken)) {
            throw new ValidationException(ErrorCode.SHARING_TOKEN_INVALID);
        }
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

    void assertGroupMemberPermission(Group group) {
        log.info("[assertGroupMemberPermission]");

        User authenticatedUser = securityHelper.getAuthenticatedUser();
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, authenticatedUser);

        if (Objects.isNull(member)) {
            log.error("[assertGroupMemberPermission] member is not in group");

            throw new AuthenticationException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
        }
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

        checkJustOnlyOneCreator(request);

        return request.stream().map(x -> groupMemberService.createGroupMember(x, group)).toList();
    }

    void checkJustOnlyOneCreator(List<GroupMemberRequest> requests) {
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
}
