package com.budgee.service;

import java.util.List;
import java.util.UUID;

import com.budgee.model.Group;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;

public interface GroupService {

    GroupResponse createGroup(GroupRequest request);

    Group getGroupById(UUID groupId);

    GroupResponse getGroup(UUID id);

    List<GroupResponse> getListGroups();
}
