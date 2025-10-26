package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.mapper.GroupMemberMapper;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.GroupTransaction;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.response.group.GroupMemberResponse;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.GroupMemberService;
import com.budgee.util.GroupTransactionHelper;
import com.budgee.util.SecurityHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-MEMBER-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberServiceImpl implements GroupMemberService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupTransactionRepository groupTransactionRepository;

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
    GroupTransactionHelper groupTransactionHelper;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public GroupMember createGroupMember(GroupMemberRequest request, Group group) {
        log.info("[createGroupMember]={}", request);

        User authenticatedUser = securityHelper.getAuthenticatedUser();
        Boolean isCreator = request.isCreator();

        final GroupRole ROLE_FOR_MEMBER = isCreator ? GroupRole.ADMIN : GroupRole.MEMBER;

        GroupMember member = this.createMember(request, group);
        member.setRole(ROLE_FOR_MEMBER);
        member.setUser(isCreator ? authenticatedUser : null);

        return member;
    }

    @Override
    public GroupMemberResponse toGroupMemberResponse(GroupMember member, Group group) {
        log.info("[toGroupMemberResponse]");

        Boolean isCreator = Objects.equals(group.getCreator(), member.getUser());

        List<GroupTransaction> transactions =
                getAllGroupTransactionsByGroupAndMember(group, member);
        BigDecimal totalSponsorship =
                groupTransactionHelper.calculateTotalSponsorship(transactions);
        BigDecimal totalAdvanceAmount =
                groupTransactionHelper.calculateAdvancePaymentFromMember(transactions);

        return groupMemberMapper.toGroupMemberResponse(
                member, isCreator, totalSponsorship, totalAdvanceAmount);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    List<GroupTransaction> getAllGroupTransactionsByGroupAndMember(
            Group group, GroupMember member) {
        log.info("[getAllGroupTransactionsByGroupAndMember]");

        return groupTransactionRepository.findAllByGroupAndMember(group, member);
    }

    GroupMember createMember(GroupMemberRequest request, Group group) {
        log.info("[createMember]={}", request);

        return groupMemberMapper.toGroupMember(request, group);
    }
}
