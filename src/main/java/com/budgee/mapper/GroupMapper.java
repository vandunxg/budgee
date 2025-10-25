package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Group;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    Group toGroup(GroupRequest request);

    @Mapping(target = "groupId", source = "id")
    @Mapping(target = "groupName", source = "name")
    GroupResponse toGroupResponse(Group group);
}
