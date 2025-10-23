package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.GroupTransactionMapper;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.GroupTransaction;
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

    // -------------------------------------------------------------------
    // IMPLEMENT METHODS
    // -------------------------------------------------------------------

    @Override
    public GroupTransactionResponse createGroupTransaction(
            UUID groupID, GroupTransactionRequest request) {
        log.info("[createGroupTransaction] groupId={} request={}", groupID, request);

        Group group = groupHelper.getGroupById(groupID);
        GroupMember member = getGroupMemberById(request.memberId());

        checkMemberInGroup(group, member);

        GroupTransaction transaction = groupTransactionMapper.toGroupTransaction(request);
        transaction.setGroup(group);
        transaction.setMember(member);

        log.warn("[createGroupTransaction] save transaction to db");
        groupTransactionRepository.save(transaction);

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

    void checkMemberInGroup(Group group, GroupMember member) {
        log.info("[checkMemberInGroup] group={} member={}", group.getId(), member.getId());

        List<GroupMember> members = groupMemberRepository.findAllByGroup(group);

        boolean isMemberInGroup = members.contains(member);

        if (!isMemberInGroup) {
            log.error(
                    "[checkMemberInGroup] member={} is not in group={}",
                    member.getId(),
                    group.getId());

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
