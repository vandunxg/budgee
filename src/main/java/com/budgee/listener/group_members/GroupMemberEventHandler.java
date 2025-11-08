package com.budgee.listener.group_members;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.budgee.event.application.AcceptedJoinGroupEvent;
import com.budgee.service.GroupMemberService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-MEMBER-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupMemberEventHandler {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    GroupMemberService groupMemberService;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAcceptedJoinRequest(AcceptedJoinGroupEvent event) {
        UUID groupId = event.groupId();
        UUID userId = event.userId();
        log.info("[onAcceptedJoinRequest] groupId={} userId={}", groupId, userId);

        groupMemberService.createGroupMember(groupId, userId);
    }
}
