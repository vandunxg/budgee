package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.response.group.GroupMemberResponse;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {

    @Mapping(target = "joinedAt", expression = "java( java.time.LocalDateTime.now() )")
    @Mapping(target = "balanceOwed", expression = "java( java.math.BigDecimal.ZERO )")
    GroupMember toGroupMember(GroupMemberRequest request, Group group);

    @Mapping(target = "memberId", source = "member.id")
    GroupMemberResponse toGroupMemberResponse(GroupMember member);
}
