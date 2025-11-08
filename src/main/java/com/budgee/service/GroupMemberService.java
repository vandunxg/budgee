package com.budgee.service;

import java.util.UUID;

public interface GroupMemberService {

    void createGroupMember(UUID groupId, UUID userId);
}
