package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.budgee.enums.GroupExpenseSource;
import com.budgee.event.application.GroupTransactionCreatedEvent;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.GroupTransactionMapper;
import com.budgee.model.*;
import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.group.CreatorTransactionResponse;
import com.budgee.payload.response.group.GroupTransactionResponse;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.GroupTransactionService;
import com.budgee.service.lookup.GroupLookup;
import com.budgee.service.validator.GroupTransactionValidator;

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
    // PUBLISHER
    // -------------------------------------------------------------------
    ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GroupTransactionMapper groupTransactionMapper;

    // -------------------------------------------------------------------
    // VALIDATOR
    // -------------------------------------------------------------------
    GroupTransactionValidator groupTransactionValidator;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    GroupLookup groupLookup;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Transactional
    @Override
    public CompletableFuture<GroupTransactionResponse> createGroupTransaction(
            UUID groupId, GroupTransactionRequest request) {
        log.info("[createGroupTransaction] groupId={} request={}", groupId, request);

        Group group = groupLookup.getGroupById(groupId);
        GroupMember member = getGroupMemberById(request.memberId());

        groupTransactionValidator.validateAuthenticatedUserIsGroupMember(group);
        groupTransactionValidator.validateMemberBelongsToGroup(group, member.getId());

        GroupTransaction transaction =
                groupTransactionMapper.toGroupTransaction(request, group, member);

        adjustGroupBalance(request, group);

        log.warn("[createGroupTransaction] save transaction to db");
        groupTransactionRepository.save(transaction);

        eventPublisher.publishEvent(new GroupTransactionCreatedEvent(groupId, transaction.getId()));

        return CompletableFuture.completedFuture(toGroupTransactionResponse(transaction));
    }

    @Override
    public GroupTransactionResponse getGroupTransaction(UUID groupId, UUID transactionId) {
        log.info("[getGroupTransaction] groupId={} transactionId={}", groupId, transactionId);

        Group group = groupLookup.getGroupById(groupId);
        groupTransactionValidator.validateAuthenticatedUserIsGroupMember(group);

        GroupTransaction transaction =
                groupTransactionRepository
                        .findById(transactionId)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND));

        return toGroupTransactionResponse(transaction);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void adjustGroupBalance(GroupTransactionRequest request, Group group) {
        log.info("[adjustGroupBalance]");

        BigDecimal amount = request.amount();

        switch (request.type()) {
            case INCOME, CONTRIBUTE -> group.increase(amount);
            case EXPENSE -> {
                if (GroupExpenseSource.GROUP_FUND.equals(request.groupExpenseSource())) {
                    group.decrease(amount);
                }
            }
            default -> log.warn(
                    "[adjustGroupBalance] Unsupported transaction type: {}", request.type());
        }
    }

    GroupTransactionResponse toGroupTransactionResponse(GroupTransaction transaction) {
        log.info("[toGroupTransactionResponse]");

        CreatorTransactionResponse creator = toCreatorTransactionResponse(transaction.getMember());
        GroupTransactionResponse response =
                groupTransactionMapper.toGroupTransactionResponse(transaction);

        response.setCreator(creator);

        return response;
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
