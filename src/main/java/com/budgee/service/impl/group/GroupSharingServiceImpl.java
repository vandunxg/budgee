package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.enums.GroupSharingStatus;
import com.budgee.model.Group;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;
import com.budgee.payload.response.group.GroupSharingResponse;
import com.budgee.payload.response.group.GroupSharingTokenResponse;
import com.budgee.payload.response.group.JoinGroupRequestResponse;
import com.budgee.repository.GroupRepository;
import com.budgee.repository.GroupSharingRepository;
import com.budgee.service.GroupSharingService;
import com.budgee.service.lookup.GroupLookup;
import com.budgee.util.AuthContext;
import com.budgee.util.CodeGenerator;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupSharingServiceImpl implements GroupSharingService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;
    GroupSharingRepository groupSharingRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    AuthContext authContext;

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------
    CodeGenerator codeGenerator;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    GroupLookup groupLookup;

    static final int SHARING_TOKEN_LENGTH = 5;

    @Override
    public GroupSharingTokenResponse generateToken(UUID groupId) {
        log.info("[generateToken] groupId={}", groupId);

        Group group = groupLookup.getGroupById(groupId);

        if (group.getIsSharing()) {
            return GroupSharingTokenResponse.builder()
                    .groupId(group.getId())
                    .token(group.getSharingToken())
                    .build();
        }

        String token = codeGenerator.generateGroupInviteToken(SHARING_TOKEN_LENGTH);

        group.setIsSharing(true);
        group.setSharingToken(token);

        log.warn("[generateToken] update groupId={} to db", group.getId());
        groupRepository.save(group);

        return GroupSharingTokenResponse.builder().token(token).groupId(group.getId()).build();
    }

    @Transactional
    @Override
    public GroupSharingResponse joinGroup(UUID groupId, String token) {
        log.info("[joinGroup] groupId={} token={}", groupId, token);

        Group group = groupLookup.getGroupById(groupId);
        User user = authContext.getAuthenticatedUser();

        group.ensureNotCreator(user);
        group.ensureSharingEnabled();
        group.validateToken(token);

        GroupSharing sharing =
                GroupSharing.builder()
                        .role(GroupRole.MEMBER)
                        .status(GroupSharingStatus.PENDING)
                        .group(group)
                        .sharedUser(user)
                        .sharingToken(token)
                        .joinedAt(LocalDateTime.now())
                        .build();

        log.warn("[joiningGroup] save sharing to db");
        groupSharingRepository.save(sharing);

        return GroupSharingResponse.builder()
                .groupId(group.getId())
                .status(sharing.getStatus())
                .build();
    }

    public List<JoinGroupRequestResponse> getJoinList(UUID groupId) {
        log.info("[getJoinList]");

        Group group = groupLookup.getGroupById(groupId);
        User authenticatedUser = authContext.getAuthenticatedUser();

        group.ensureCreator(authenticatedUser);

        List<GroupSharing> joinRequests = getJoinRequestsByGroup(group);

        return joinRequests.stream().map(this::mapToJoinGroupRequestResponse).toList();
    }

    List<GroupSharing> getJoinRequestsByGroup(Group group) {
        log.info("[getJoinRequestsByGroup]");

        return groupSharingRepository.findAllByGroup(group);
    }

    JoinGroupRequestResponse mapToJoinGroupRequestResponse(GroupSharing groupSharing) {
        log.info("[mapToJoinGroupRequestResponse]");

        User user = groupSharing.getSharedUser();

        return JoinGroupRequestResponse.builder()
                .fullName(user.getFullName())
                .userId(user.getId())
                .joinedAt(groupSharing.getJoinedAt())
                .build();
    }
}
