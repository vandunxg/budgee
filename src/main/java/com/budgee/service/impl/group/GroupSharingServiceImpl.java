package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.enums.GroupSharingStatus;
import com.budgee.event.application.AcceptedJoinGroupEvent;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.model.Group;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;
import com.budgee.payload.request.group.AcceptJoinRequest;
import com.budgee.payload.response.group.GroupSharingResponse;
import com.budgee.payload.response.group.GroupSharingTokenResponse;
import com.budgee.payload.response.group.JoinGroupRequestResponse;
import com.budgee.repository.GroupRepository;
import com.budgee.repository.GroupSharingRepository;
import com.budgee.service.GroupSharingService;
import com.budgee.service.lookup.GroupLookup;
import com.budgee.service.lookup.UserLookup;
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
    UserLookup userLookup;

    // -------------------------------------------------------------------
    // PUBLISHER
    // -------------------------------------------------------------------
    ApplicationEventPublisher eventPublisher;

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

        if (group.checkUserIsMember(user)) {
            log.warn("[joinGroup] user is member of this group, cant join again");

            throw new ValidationException(ErrorCode.USER_IN_GROUP);
        }

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
        GroupSharingStatus status = GroupSharingStatus.PENDING;

        group.ensureCreator(authenticatedUser);

        List<GroupSharing> joinRequests = getJoinRequestsByGroupAndPendingStatus(group, status);

        return joinRequests.stream().map(this::mapToJoinGroupRequestResponse).toList();
    }

    @Transactional
    @Override
    public void acceptJoinRequest(UUID groupId, AcceptJoinRequest request) {
        log.info("[acceptJoinRequest] groupId={} userId={}", groupId, request.userId());

        Group group = groupLookup.getGroupById(groupId);
        User sharedUser = userLookup.getUserById(request.userId());

        GroupSharing sharing = groupSharingRepository.findByGroupAndSharedUser(group, sharedUser);

        sharing.ensureGroupSharingNotNull(sharing);

        if (sharing.isAccepted()) {
            log.warn("[acceptJoinRequest] sharing is accepted, can't accept again");

            throw new ValidationException(ErrorCode.INVALID_REQUEST_JOINING_GROUP);
        }

        sharing.markAccepted();

        log.info("[acceptJoinRequest] update status sharing to db");
        groupSharingRepository.save(sharing);

        eventPublisher.publishEvent(new AcceptedJoinGroupEvent(groupId, request.userId()));
    }

    List<GroupSharing> getJoinRequestsByGroupAndPendingStatus(
            Group group, GroupSharingStatus status) {
        log.info(
                "[getJoinRequestsByGroupAndPendingStatus] groupId={} status={}",
                group.getId(),
                status);

        return groupSharingRepository.findAllByGroupAndStatus(group, status);
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
