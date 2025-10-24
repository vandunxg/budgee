package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Group;
import com.budgee.model.User;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(source = "user", target = "creator")
    Group toGroup(GroupRequest request, User user);

    @Mapping(target = "groupId", source = "id")
    GroupResponse toGroupResponse(Group group);
}
