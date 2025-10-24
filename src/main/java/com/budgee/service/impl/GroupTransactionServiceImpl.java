package com.budgee.service.impl;

import com.budgee.exception.ValidationException;
import com.budgee.model.*;
import com.budgee.util.SecurityHelper;
import com.budgee.util.UserHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.GroupTransactionMapper;
import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.group.CreatorTransactionResponse;
import com.budgee.payload.response.group.GroupTransactionResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.GroupTransactionService;
import com.budgee.util.GroupHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-TRANSACTION-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupTransactionServiceImpl implements GroupTransactionService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupMemberRepository groupMemberRepository;
    GroupTransactionRepository groupTransactionRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GroupTransactionMapper groupTransactionMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    GroupHelper groupHelper;
    UserHelper userHelper;
    SecurityHelper securityHelper;

    // -------------------------------------------------------------------
    // IMPLEMENT METHODS
    // -------------------------------------------------------------------

    @Override
    public GroupTransactionResponse createGroupTransaction(
            UUID groupID, GroupTransactionRequest request) {
        log.info("[createGroupTransaction] groupId={} request={}", groupID, request);

        Group group = groupHelper.getGroupById(groupID);
        GroupMember member = getGroupMemberById(request.memberId());

        checkMemberInGroup(group, member.getId());

        GroupTransaction transaction = groupTransactionMapper.toGroupTransaction(request);
        transaction.setGroup(group);
        transaction.setMember(member);

        log.warn("[createGroupTransaction] save transaction to db");
        groupTransactionRepository.save(transaction);

        return toGroupTransactionResponse(transaction);
    }

    @Override
    public GroupTransactionResponse getGroupTransaction(UUID groupId, UUID transactionId) {
        log.info("[getGroupTransaction] groupId={} transactionId={}", groupId, transactionId);

        Group group = groupHelper.getGroupById(groupId);
        checkAuthenticatedUserIsMemberOfGroup(group);

        GroupTransaction transaction = groupTransactionRepository.findById(transactionId).orElseThrow(
                () -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND)
        );

        return toGroupTransactionResponse(transaction);
    }

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------

    GroupTransactionResponse toGroupTransactionResponse(GroupTransaction transaction) {
        log.info("[toGroupTransactionResponse]");

        CreatorTransactionResponse creator = toCreatorTransactionResponse(transaction.getMember());
        GroupTransactionResponse response =
                groupTransactionMapper.toGroupTransactionResponse(transaction);

        response.setCreator(creator);

        return response;
    }

    void checkAuthenticatedUserIsMemberOfGroup(Group group) {
        log.info("[getMemberByAuthenticatedUser]");

        User authenticatedUser = securityHelper.getAuthenticatedUser();

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, authenticatedUser);

        if(Objects.isNull(member)) {
            throw new ValidationException(ErrorCode.USER_NOT_IN_GROUP);
        }
    }

    void checkMemberInGroup(Group group, UUID memberId) {
        log.info("[checkMemberInGroup] group={} member={}", group.getId(), memberId);

        GroupMember member = groupMemberRepository.findByGroupAndId(group, memberId);

        if(Objects.isNull(member)) {
            log.error("[checkMemberInGroup] member is not in this group");

            throw new NotFoundException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
        }
    }

    GroupMember getGroupMemberById(UUID memberId) {
        log.info("[memberId]={}", memberId);

        return groupMemberRepository
                .findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
    }

    CreatorTransactionResponse toCreatorTransactionResponse(GroupMember member) {
        log.info("[toCreatorTransactionResponse] memberId={}", member.getId());

        return CreatorTransactionResponse.builder()
                .creatorName(member.getMemberName())
                .creatorId(member.getId())
                .build();
    }
}
