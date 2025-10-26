package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.enums.GroupSharingStatus;
import com.budgee.model.Group;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;
import com.budgee.repository.GroupSharingRepository;
import com.budgee.service.GroupSharingService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-SHARING-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupSharingServiceImpl implements GroupSharingService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupSharingRepository groupSharingRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public void createGroupSharing(User user, Group group, String sharingToken) {
        log.info("[createGroupSharing]");

        final GroupRole role = GroupRole.MEMBER;
        final GroupSharingStatus status = GroupSharingStatus.PENDING;

        GroupSharing groupSharing =
                GroupSharing.builder()
                        .role(role)
                        .sharedUser(user)
                        .group(group)
                        .status(status)
                        .sharingToken(sharingToken)
                        .joinedAt(LocalDateTime.now())
                        .build();

        log.warn("[createGroupSharing] save group sharing to db");
        groupSharingRepository.save(groupSharing);
    }

    @Override
    public GroupSharing getGroupSharingByUserAndGroup(User user, Group group) {
        log.info("[getGroupSharingByUserAndGroup]");

        return groupSharingRepository.findByGroupAndSharedUser(group, user);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------
}
