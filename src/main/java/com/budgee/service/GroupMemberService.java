package com.budgee.service;

import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.response.group.GroupMemberResponse;

public interface GroupMemberService {

    GroupMember createGroupMember(GroupMemberRequest request, Group group);

    GroupMemberResponse toGroupMemberResponse(GroupMember member, Group group);
}
