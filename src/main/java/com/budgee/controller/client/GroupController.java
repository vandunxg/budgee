package com.budgee.controller.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.service.GroupService;
import com.budgee.service.GroupTransactionService;
import com.budgee.util.MessageConstants;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupController {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------

    GroupService groupService;
    GroupTransactionService groupTransactionService;

    // -------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------

    @PostMapping("/")
    ResponseEntity<?> createGroup(@RequestBody GroupRequest request) {
        log.info("[POST /groups/]={}", request);

        return ResponseUtil.created(groupService.createGroup(request));
    }

    @GetMapping("/{id}")
    ResponseEntity<?> getGroup(@PathVariable UUID id) {
        log.info("[GET /group/{}]", id);

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, groupService.getGroup(id));
    }

    @GetMapping("/{id}/transactions/")
    ResponseEntity<?> getGroupTransaction(@PathVariable UUID id) {
        log.info("[GET groups/{}/transactions/]", id);

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, groupService.getGroup(id));
    }

    @PostMapping("/{id}/transactions/")
    ResponseEntity<?> createGroupTransaction(
            @PathVariable UUID id, @RequestBody GroupTransactionRequest request) {
        log.info("[POST /groups/{}/transactions/]={}", id, request);

        return ResponseUtil.created(groupTransactionService.createGroupTransaction(id, request));
    }

    @GetMapping("/{groupId}/transactions/{transactionId}")
    ResponseEntity<?> getGroupTransaction(
            @PathVariable UUID groupId, @PathVariable UUID transactionId) {
        log.info("[GET /groups/{}/transactions/{}]", groupId, transactionId);

        return ResponseUtil.success(
                MessageConstants.FETCH_SUCCESS,
                groupTransactionService.getGroupTransaction(groupId, transactionId));
    }

    @GetMapping("/list")
    ResponseEntity<?> getListGroups() {
        log.info("[GET /groups/list]");

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, groupService.getListGroups());
    }

    @GetMapping("/{id}/sharing")
    ResponseEntity<?> getGroupSharingToken(@PathVariable UUID id) {
        log.info("[GET /{}/sharing]", id);

        return ResponseUtil.success(groupService.getGroupSharingToken(id));
    }

    @GetMapping("/{id}/join")
    ResponseEntity<?> joinGroupSharing(
            @RequestParam(name = "sharing-token") String sharingToken, @PathVariable UUID id) {
        log.info("[GET /groups/{}/join?sharing-token={}]", id, sharingToken);

        return ResponseUtil.success(
                MessageConstants.JOIN_GROUP_SUCCESS, groupService.joinGroup(id, sharingToken));
    }

    @GetMapping("/{id}/join-list")
    ResponseEntity<?> joinList(@PathVariable UUID id) {
        log.info("[GET /groups/{}/join-list]", id);

        return ResponseUtil.success(
                MessageConstants.FETCH_SUCCESS,
                groupService.getJoinList(id)
        );
    }
}
