package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.factory.GroupMemberFactory;
import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;
import com.budgee.repository.GroupRepository;
import com.budgee.service.GroupMemberService;
import com.budgee.service.lookup.GroupLookup;
import com.budgee.service.lookup.UserLookup;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-MEMBER-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberServiceImpl implements GroupMemberService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;

    // -------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------
    GroupMemberFactory groupMemberFactory;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    GroupLookup groupLookup;
    UserLookup userLookup;

    @Transactional
    @Override
    public void createGroupMember(UUID groupId, UUID userId) {
        log.info("[createGroupMember] groupId={} userId={}", groupId, userId);

        User user = userLookup.getUserById(userId);
        Group group = groupLookup.getGroupById(groupId);

        GroupMember newMember = groupMemberFactory.createGroupMemberWithRoleMember(group, user);
        group.addMemberToGroup(newMember);

        groupRepository.save(group);
    }
}
