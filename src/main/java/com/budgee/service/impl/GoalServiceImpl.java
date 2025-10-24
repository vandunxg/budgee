package com.budgee.service.impl;

import io.jsonwebtoken.lang.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.exception.ValidationException;
import com.budgee.mapper.GoalMapper;
import com.budgee.model.*;
import com.budgee.payload.request.GoalRequest;
import com.budgee.payload.response.GoalResponse;
import com.budgee.repository.GoalRepository;
import com.budgee.service.CategoryService;
import com.budgee.service.GoalService;
import com.budgee.service.UserService;
import com.budgee.util.DateValidator;
import com.budgee.util.SecurityHelper;
import com.budgee.util.WalletHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GOAL-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalServiceImpl implements GoalService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GoalRepository goalRepository;

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------
    UserService userService;
    CategoryService categoryService;

    // -------------------------------------------------------------------
    // MAPPER / HELPER
    // -------------------------------------------------------------------
    GoalMapper goalMapper;
    DateValidator dateValidator;
    SecurityHelper securityHelper;
    WalletHelper walletHelper;

    // -------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        log.info("[createGoal] request={}", request);

        User user = userService.getCurrentUser();
        validateDates(request);

        Goal goal = goalMapper.toGoal(request, user);

        // Map related entities
        goal.setGoalWallets(buildGoalWallets(request.wallets(), goal, user));
        goal.setGoalCategories(buildGoalCategories(request.categories(), goal, user));

        goalRepository.save(goal);

        return goalMapper.toGoalResponse(goal);
    }

    @Override
    public GoalResponse getGoal(UUID id) {
        log.info("[getGoal] id={}", id);

        Goal goal = getGoalById(id);
        securityHelper.checkIsOwner(goal);

        return goalMapper.toGoalResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID id, GoalRequest request) {
        log.info("[updateGoal] id={} request={}", id, request);

        User user = userService.getCurrentUser();
        Goal goal = getGoalOfCurrentUserById(id);

        validateDates(request);

        updateGoalEntity(goal, request, user);
        return goalMapper.toGoalResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID id) {
        log.info("[deleteGoal] id={}", id);

        Goal goal = getGoalOfCurrentUserById(id);
        goal.getGoalWallets().clear();
        goal.getGoalCategories().clear();

        goalRepository.delete(goal);
        log.warn("[deleteGoal] deleted id={}", id);
    }

    @Override
    public List<GoalResponse> getListGoals() {
        log.info("[getListGoals]");

        User user = userService.getCurrentUser();
        List<Goal> goals = goalRepository.findAllByUser(user);
        return goals.stream().map(goalMapper::toGoalResponse).toList();
    }

    // -------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------

    void validateDates(GoalRequest request) {
        log.info("[validateDates]");

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());
    }

    List<GoalWallet> buildGoalWallets(List<UUID> walletIds, Goal goal, User user) {
        log.info("[buildGoalWallets]");

        if (walletIds == null || walletIds.isEmpty()) {
            log.error("[buildGoalWallets] walletIds is null");

            throw new ValidationException(ErrorCode.WALLET_IS_REQUIRED);
        }

        return walletIds.stream()
                .map(walletHelper::getWalletByIdForOwner)
                .map(wallet -> GoalWallet.builder().goal(goal).wallet(wallet).user(user).build())
                .toList();
    }

    List<GoalCategory> buildGoalCategories(List<UUID> categoryIds, Goal goal, User user) {
        log.info("[buildGoalCategories]");

        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ValidationException(ErrorCode.CATEGORY_IS_REQUIRED);
        }

        return categoryIds.stream()
                .map(categoryService::getCategoryByIdForOwner)
                .map(
                        category ->
                                GoalCategory.builder()
                                        .goal(goal)
                                        .category(category)
                                        .user(user)
                                        .build())
                .toList();
    }

    void updateGoalEntity(Goal goal, GoalRequest request, User user) {
        log.info(
                "[updateGoalEntity] goalId={} request={} userId={}",
                goal.getId(),
                request,
                user.getId());

        updateGoalName(goal, request.name());
        updateGoalTargetAmount(goal, request.targetAmount());
        updateGoalStartDate(goal, request.startDate());
        updateGoalEndDate(goal, request.endDate());
        updateGoalWallets(goal, request, user);
        updateGoalCategories(goal, request, user);
    }

    void updateGoalCategories(Goal goal, GoalRequest request, User user) {

        goal.getGoalCategories().clear();
        goal.getGoalCategories().addAll(buildGoalCategories(request.categories(), goal, user));
    }

    void updateGoalWallets(Goal goal, GoalRequest request, User user) {

        goal.getGoalWallets().clear();
        goal.getGoalWallets().addAll(buildGoalWallets(request.wallets(), goal, user));
    }

    void updateGoalName(Goal goal, String newName) {

        if (!Strings.hasText(newName)) return;

        goal.setName(newName);
    }

    void updateGoalTargetAmount(Goal goal, BigDecimal newTargetAmount) {

        if (BigDecimal.ZERO.equals(newTargetAmount)) {
            return;
        }

        goal.setTargetAmount(newTargetAmount);
    }

    void updateGoalStartDate(Goal goal, LocalDate newStartDate) {

        goal.setStartDate(newStartDate);
    }

    void updateGoalEndDate(Goal goal, LocalDate newEndDate) {

        goal.setEndDate(newEndDate);
    }

    Goal getGoalById(UUID id) {
        log.info("[getGoalById]={}", id);

        return goalRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND));
    }

    Goal getGoalOfCurrentUserById(UUID id) {
        log.info("[getGoalOfCurrentUserById]={}", id);

        Goal goal = getGoalById(id);

        securityHelper.checkIsOwner(goal);

        return goal;
    }
}
