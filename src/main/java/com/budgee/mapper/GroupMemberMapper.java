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

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "isCreator", source = "isCreator")
    @Mapping(target = "totalSponsorship", source = "totalSponsorship")
    @Mapping(target = "totalAdvanceAmount", source = "totalAdvanceAmount")
    GroupMemberResponse toGroupMemberResponse(
            GroupMember member,
            Boolean isCreator,
            BigDecimal totalSponsorship,
            BigDecimal totalAdvanceAmount);
}
