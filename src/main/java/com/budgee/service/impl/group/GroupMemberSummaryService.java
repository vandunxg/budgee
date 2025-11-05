package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.budgee.model.GroupMember;
import com.budgee.model.GroupTransaction;
import com.budgee.payload.response.group.GroupMemberResponse;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-MEMBER-SUMMARY-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberSummaryService {

    public GroupMemberResponse calculateGroupMemberSummary(
            GroupMember member, boolean isCreator, List<GroupTransaction> transactions) {
        log.info("[calculateGroupMemberSummary] memberId={}", member.getId());

        BigDecimal totalSponsorship =
                transactions.stream()
                        .filter(GroupTransaction::isMemberSponsorSource)
                        .map(GroupTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAdvanceAmount =
                transactions.stream()
                        .filter(GroupTransaction::isMemberAdvanceSource)
                        .map(GroupTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info(
                "[calculateGroupMemberSummary] member={} isCreator={} sponsorship={} advance={}",
                member.getId(),
                isCreator,
                totalSponsorship,
                totalAdvanceAmount);

        return GroupMemberResponse.builder()
                .isCreator(isCreator)
                .memberId(member.getId())
                .memberName(member.getMemberName())
                .totalAdvanceAmount(totalAdvanceAmount)
                .totalSponsorship(totalSponsorship)
                .build();
    }
}
