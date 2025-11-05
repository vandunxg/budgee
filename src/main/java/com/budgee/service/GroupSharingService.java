package com.budgee.service;

import java.util.List;
import java.util.UUID;

import com.budgee.payload.response.group.GroupSharingResponse;
import com.budgee.payload.response.group.GroupSharingTokenResponse;
import com.budgee.payload.response.group.JoinGroupRequestResponse;

public interface GroupSharingService {

    GroupSharingTokenResponse generateToken(UUID groupID);

    GroupSharingResponse joinGroup(UUID groupId, String token);

    List<JoinGroupRequestResponse> getJoinList(UUID groupId);
}
