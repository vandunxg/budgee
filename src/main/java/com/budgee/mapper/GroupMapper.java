package com.budgee.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Group;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupMemberResponse;
import com.budgee.payload.response.group.GroupResponse;
import com.budgee.payload.response.group.GroupSummary;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "creator", source = "user")
    Group toGroup(GroupRequest request, User user);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "summary", source = "summary")
    @Mapping(target = "members", expression = "java(members)")
    GroupResponse toGroupResponse(
            Group group, GroupSummary summary, List<GroupMemberResponse> members);
}
