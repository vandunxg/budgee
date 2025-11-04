package com.budgee.service.lookup;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.model.GroupMember;
import com.budgee.repository.GroupMemberRepository;

@Component
@Slf4j(topic = "GROUP-MEMBER-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupMemberRepository groupMemberRepository;

    public List<GroupMember> getAllGroupMembersByGroupId(UUID groupId) {
        log.info("[getAllGroupMembersByGroupId] groupId={}", groupId);

        return groupMemberRepository.findAllWithUserByGroupId(groupId);
    }
}
