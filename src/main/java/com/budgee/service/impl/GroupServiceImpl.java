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
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.enums.TransactionType;
import com.budgee.exception.AuthenticationException;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.exception.ValidationException;
import com.budgee.mapper.GroupMapper;
import com.budgee.model.*;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupRepository;
import com.budgee.repository.GroupTransactionRepository;
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
    GroupTransactionRepository groupTransactionRepository;

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

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

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

        GroupResponse response = groupMapper.toGroupResponse(group);
        response.setMembers(
                group.getMembers().stream()
                        .map(groupMemberService::toGroupMemberResponse)
                        .toList());

        calculateTotalSponsorship(transactions, response);

        return response;
    }

    void calculateTotalSponsorship(List<GroupTransaction> transactions, GroupResponse response) {
        log.info("[calculateTotalSponsorship]");

        BigDecimal totalSponsorshipFromExpense =
                transactions.stream()
                        .filter(
                                x ->
                                        TransactionType.EXPENSE.equals(x.getType())
                                                && GroupExpenseSource.MEMBER_SPONSOR.equals(
                                                        x.getGroupExpenseSource()))
                        .map(GroupTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSponsorshipFromMember =
                transactions.stream()
                        .filter(x -> TransactionType.CONTRIBUTE.equals(x.getType()))
                        .map(GroupTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSponsorshipOfGroup =
                totalSponsorshipFromExpense.add(totalSponsorshipFromMember);

        response.setTotalSponsorship(totalSponsorshipOfGroup);
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
