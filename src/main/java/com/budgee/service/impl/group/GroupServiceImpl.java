package com.budgee.service.impl.group;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.budgee.event.application.GroupDeletedEvent;
import com.budgee.exception.*;
import com.budgee.factory.GroupFactory;
import com.budgee.mapper.GroupMapper;
import com.budgee.model.*;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.*;
import com.budgee.repository.GroupMemberRepository;
import com.budgee.repository.GroupRepository;
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
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;
    GroupMemberRepository groupMemberRepository;
    GroupTransactionRepository groupTransactionRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    AuthContext authContext;
    GroupSummaryService groupSummaryService;
    GroupMemberSummaryService groupMemberSummaryService;

    // -------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------
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
    // PUBLISHER
    // -------------------------------------------------------------------
    ApplicationEventPublisher eventPublisher;

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
    public Void deleteGroup(UUID id) {
        log.info("[deleteGroup]={}", id);

        Group group = getGroupById(id);
        User authenticatedUser = authContext.getAuthenticatedUser();

        group.checkIsOwner(authenticatedUser);

        eventPublisher.publishEvent(
                new GroupDeletedEvent(group.getId(), authenticatedUser.getId()));

        log.warn("[deleteGroup] delete groupId={} from db", id);
        groupRepository.delete(group);

        return null;
    }

    @Override
    public List<GroupResponse> getListGroups() {
        log.info("[getListGroups]");

        List<Group> groups = findAllGroupByAuthenticatedUser();

        return toListGroupResponse(groups);
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    List<GroupResponse> toListGroupResponse(List<Group> groups) {
        log.info("[toListGroupResponse]");

        return groups.stream()
                .map(
                        group -> {
                            GroupSummary summary = groupSummaryService.calculateGroupSummary(group);

                            return groupMapper.toGroupResponse(group, summary, null);
                        })
                .toList();
    }

    List<Group> findAllGroupByAuthenticatedUser() {
        log.info("[findAllGroupByAuthenticatedUser]");

        User authenticatedUser = authContext.getAuthenticatedUser();

        List<GroupMember> members = groupMemberRepository.findAllByUser(authenticatedUser);

        return members.stream().map(GroupMember::getGroup).toList();
    }

    public GroupResponse getGroupResponse(UUID groupId) {
        log.info("[getGroupResponse] groupId={}", groupId);

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
