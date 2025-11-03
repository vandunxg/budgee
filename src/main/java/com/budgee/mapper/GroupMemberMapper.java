package com.budgee.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.enums.GroupRole;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.response.group.GroupMemberResponse;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {

    @Mapping(target = "joinedAt", expression = "java( java.time.LocalDateTime.now() )")
    @Mapping(target = "balanceOwed", expression = "java( java.math.BigDecimal.ZERO )")
    GroupMember toGroupMember(GroupMemberRequest request, Group group, GroupRole role);

    @Mapping(target = "memberId", expression = "java(member.getId())")
    @Mapping(target = "memberName", expression = "java(member.getMemberName())")
    @Mapping(target = "isCreator", expression = "java(isCreator)")
    @Mapping(
            target = "totalSponsorship",
            expression =
                    "java(totalSponsorship != null ? totalSponsorship : java.math.BigDecimal.ZERO)")
    @Mapping(
            target = "totalAdvanceAmount",
            expression =
                    "java(totalAdvanceAmount != null ? totalAdvanceAmount : java.math.BigDecimal.ZERO)")
    GroupMemberResponse toGroupMemberResponse(
            GroupMember member,
            Boolean isCreator,
            BigDecimal totalSponsorship,
            BigDecimal totalAdvanceAmount);
}
