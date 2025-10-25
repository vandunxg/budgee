package com.budgee.service;

import java.util.List;
import java.util.UUID;

import com.budgee.model.Group;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;
import com.budgee.payload.response.group.GroupSharingTokenResponse;

public interface GroupService {

    GroupResponse createGroup(GroupRequest request);

    Group getGroupById(UUID groupId);

    GroupResponse getGroup(UUID id);

    List<GroupResponse> getListGroups();

    GroupSharingTokenResponse getGroupSharingToken(UUID groupId);

    Void joinGroup(UUID groupId, String sharingToken);
}
