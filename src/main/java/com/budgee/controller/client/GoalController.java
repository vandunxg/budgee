package com.budgee.controller.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.GoalRequest;
import com.budgee.service.GoalService;
import com.budgee.util.MessageConstants;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@Slf4j(topic = "GOAL-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalController {

    GoalService goalService;

    @PostMapping("/")
    ResponseEntity<?> createGoal(@RequestBody GoalRequest request) {
        log.info("[POST /goals/]={}", request);

        return ResponseUtil.created(goalService.createGoal(request));
    }

    @GetMapping("/{id}")
    ResponseEntity<?> getGoal(@PathVariable UUID id) {
        log.info("[GET /goals/{}]", id);

        return ResponseUtil.created(goalService.getGoal(id));
    }

    @PatchMapping("/{id}")
    ResponseEntity<?> updateGoal(@PathVariable UUID id, @RequestBody GoalRequest request) {
        log.info("[PATCH /goals/{}]", id);

        return ResponseUtil.success(
                MessageConstants.UPDATE_SUCCESS, goalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteGoal(@PathVariable UUID id) {
        log.info("[DELETE /goals/{}]", id);

        goalService.deleteGoal(id);

        return ResponseUtil.deleted();
    }

    @GetMapping("/list")
    ResponseEntity<?> getListGoals() {
        log.info("[GET /goals/list/]");

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, goalService.getListGoals());
    }
}
