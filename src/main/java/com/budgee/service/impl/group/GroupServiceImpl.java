package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.enums.GroupRole;
import com.budgee.enums.GroupSharingStatus;
import com.budgee.exception.*;
import com.budgee.factory.GroupFactory;
import com.budgee.factory.GroupMemberFactory;
import com.budgee.mapper.GroupMapper;
import com.budgee.model.*;
import com.budgee.payload.request.group.GroupMemberRequest;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.*;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupRepository;
import com.budgee.repository.GroupSharingRepository;
import com.budgee.repository.GroupTransactionRepository;
import com.budgee.service.GroupService;
import com.budgee.service.validator.DateValidator;
import com.budgee.service.validator.GroupValidator;
import com.budgee.util.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupServiceImpl implements GroupService {

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------
    Integer SHARING_TOKEN_LENGTH = 5;

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;
    GroupMemberRepository groupMemberRepository;
    GroupTransactionRepository groupTransactionRepository;
    GroupSharingRepository groupSharingRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    AuthContext authContext;
    GroupSummaryService groupSummaryService;
    GroupMemberSummaryService groupMemberSummaryService;

    // -------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------
    GroupMemberFactory groupMemberFactory;
    GroupFactory groupFactory;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GroupMapper groupMapper;

    // -------------------------------------------------------------------
    // VALIDATOR
    // -------------------------------------------------------------------
    DateValidator dateValidator;
    GroupValidator groupValidator;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    CodeGenerator codeGenerator;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public GroupResponse createGroup(GroupRequest request) {
        log.info("[createGroup]={}", request);

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());
        groupValidator.validateSingleCreator(request.groupMembers());

        User authenticatedUser = authContext.getAuthenticatedUser();
        Group group = groupFactory.createGroup(request, authenticatedUser);

        groupRepository.save(group);

        return getGroupResponse(group.getId());
    }

    @Override
    public Group getGroupById(UUID groupId) {
        log.info("[getGroupById]={}", groupId);

        return groupRepository
                .findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Override
    public GroupResponse getGroup(UUID id) {
        log.info("[getGroup]={}", id);

        Group group = getGroupById(id);
        User authenticatedUser = authContext.getAuthenticatedUser();

        group.ensureCurrentUserIsMember(authenticatedUser);

        return getGroupResponse(group.getId());
    }

    @Override
    public List<GroupResponse> getListGroups() {
        log.info("[getListGroups]");

        List<Group> groups = findAllGroupByAuthenticatedUser();

        return toListGroupResponse(groups);
    }

    @Override
    public GroupSharingTokenResponse getGroupSharingToken(UUID groupId) {
        log.info("[getGroupSharingToken] groupId={}", groupId);

        Group group = getGroupById(groupId);

        if (group.getIsSharing()) {

            return mapToGroupSharingTokenResponse(group, group.getSharingToken());
        }

        String sharingToken = codeGenerator.generateGroupInviteToken(SHARING_TOKEN_LENGTH);
        setGroupSharing(group, sharingToken);

        return mapToGroupSharingTokenResponse(group, sharingToken);
    }

    @Transactional
    @Override
    public GroupSharingResponse joinGroup(UUID groupId, String sharingToken) {
        log.info("[joinGroup] groupId={} sharingToken={}", groupId, sharingToken);

        Group group = getGroupById(groupId);
        User user = authContext.getAuthenticatedUser();

        group.ensureNotCreator(user);
        group.ensureSharingEnabled();
        group.validateToken(sharingToken);

        createGroupSharing(user, group, sharingToken);

        return GroupSharingResponse.builder()
                .groupId(groupId)
                .status(GroupSharingStatus.PENDING)
                .build();
    }

    @Override
    public List<JoinGroupRequestResponse> getJoinList(UUID groupId) {
        log.info("[getJoinList]");

        Group group = getGroupById(groupId);
        User authenticatedUser = authContext.getAuthenticatedUser();

        group.ensureCreator(authenticatedUser);

        List<GroupSharing> joinRequests = getJoinRequestsByGroup(group);

        return joinRequests.stream().map(this::mapToJoinGroupRequestResponse).toList();
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void createGroupSharing(User user, Group group, String sharingToken) {
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

    JoinGroupRequestResponse mapToJoinGroupRequestResponse(GroupSharing groupSharing) {
        log.info("[mapToJoinGroupRequestResponse]");

        User user = groupSharing.getSharedUser();

        return JoinGroupRequestResponse.builder()
                .fullName(user.getFullName())
                .userId(user.getId())
                .joinedAt(groupSharing.getJoinedAt())
                .build();
    }

    List<GroupSharing> getJoinRequestsByGroup(Group group) {
        log.info("[getJoinRequestsByGroup]");

        return groupSharingRepository.findAllByGroup(group);
    }

    void setGroupSharing(Group group, String sharingToken) {
        log.info("[setGroupSharing]");

        group.setIsSharing(Boolean.TRUE);
        group.setSharingToken(sharingToken);

        log.warn("[setGroupSharing] update group to db");
        groupRepository.save(group);
    }

    GroupSharingTokenResponse mapToGroupSharingTokenResponse(Group group, String sharingToken) {
        log.info("[mapToGroupSharingTokenResponse]");

        return GroupSharingTokenResponse.builder()
                .token(sharingToken)
                .groupId(group.getId())
                .build();
    }

    List<GroupResponse> toListGroupResponse(List<Group> groups) {
        log.info("[toListGroupResponse]");

        return groups.stream()
                .map(
                        group -> {
                            GroupSummary summary = groupSummaryService.calculateGroupSummary(group);

                            return groupMapper.toGroupResponse(group, summary);
                        })
                .toList();
    }

    List<Group> findAllGroupByAuthenticatedUser() {
        log.info("[findAllGroupByAuthenticatedUser]");

        User authenticatedUser = authContext.getAuthenticatedUser();

        List<GroupMember> members = groupMemberRepository.findAllByUser(authenticatedUser);

        return members.stream().map(GroupMember::getGroup).toList();
    }

    List<GroupMember> createGroupMembers(List<GroupMemberRequest> request, Group group) {
        log.info("[createGroupMember]={}", request);

        groupValidator.validateSingleCreator(request);

        return request.stream()
                .map(memberRequest -> groupMemberFactory.createGroupMember(memberRequest, group))
                .toList();
    }

    public GroupResponse getGroupResponse(UUID groupId) {
        Group group = getGroupById(groupId);

        List<GroupTransaction> transactions = groupTransactionRepository.findAllByGroup(group);
        GroupSummary summary = groupSummaryService.calculateGroupSummary(group);

        List<GroupMemberResponse> members =
                group.getMembers().stream()
                        .map(
                                member -> {
                                    boolean isCreator =
                                            Objects.equals(group.getCreator(), member.getUser());

                                    List<GroupTransaction> memberTransactions =
                                            transactions.stream()
                                                    .filter(x -> x.isTransactionOwner(member))
                                                    .toList();

                                    return groupMemberSummaryService.calculateGroupMemberSummary(
                                            member, isCreator, memberTransactions);
                                })
                        .toList();

        return groupMapper.toGroupResponse(group, summary, members);
    }
}
