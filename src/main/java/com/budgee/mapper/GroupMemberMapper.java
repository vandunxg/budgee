package com.budgee.mapper;

import org.mapstruct.Mapper;

import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.response.group.GroupMemberResponse;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {

    GroupMember toGroupMember(GroupMemberRequest request);

    GroupMemberResponse toGroupMemberResponse(Group group);
}
