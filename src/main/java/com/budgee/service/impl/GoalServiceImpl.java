package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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
import com.budgee.service.GoalService;
import com.budgee.service.lookup.CategoryLookup;
import com.budgee.service.lookup.WalletLookup;
import com.budgee.service.validator.DateValidator;
import com.budgee.service.validator.GoalValidator;
import com.budgee.util.SecurityHelper;

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
    CategoryLookup categoryLookup;
    WalletLookup walletLookup;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    GoalMapper goalMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    SecurityHelper securityHelper;

    // -------------------------------------------------------------------
    // VALIDATOR
    // -------------------------------------------------------------------
    DateValidator dateValidator;
    GoalValidator goalValidator;

    // -------------------------------------------------------------------
    // PUBLIC METHODS
    // -------------------------------------------------------------------

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        log.info("[createGoal] request={}", request);

        User user = securityHelper.getAuthenticatedUser();
        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());

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

        User user = securityHelper.getAuthenticatedUser();
        Goal goal = getGoalForCurrentUser(id);

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());

        applyGoalUpdate(goal, request, user);

        return goalMapper.toGoalResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID id) {
        log.info("[deleteGoal] id={}", id);

        Goal goal = getGoalForCurrentUser(id);

        goal.getGoalWallets().clear();
        goal.getGoalCategories().clear();

        goalRepository.delete(goal);
        log.warn("[deleteGoal] deleted id={}", id);
    }

    @Override
    public List<GoalResponse> getListGoals() {
        log.info("[getListGoals]");

        User user = securityHelper.getAuthenticatedUser();
        List<Goal> goals = goalRepository.findAllByUser(user);

        return goals.stream().map(goalMapper::toGoalResponse).toList();
    }

    // -------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------

    List<GoalWallet> buildGoalWallets(List<UUID> walletIds, Goal goal, User user) {
        log.info("[buildGoalWallets]");

        if (walletIds == null || walletIds.isEmpty()) {
            log.error("[buildGoalWallets] walletIds is null");

            throw new ValidationException(ErrorCode.WALLET_IS_REQUIRED);
        }

        return walletIds.stream()
                .map(walletLookup::getWalletForCurrentUser)
                .map(wallet -> GoalWallet.builder().goal(goal).wallet(wallet).user(user).build())
                .toList();
    }

    List<GoalCategory> buildGoalCategories(List<UUID> categoryIds, Goal goal, User user) {
        log.info("[buildGoalCategories]");

        if (categoryIds == null || categoryIds.isEmpty()) {
            log.error("[buildGoalCategories] categoryIds is null");

            throw new ValidationException(ErrorCode.CATEGORY_IS_REQUIRED);
        }

        return categoryIds.stream()
                .map(categoryLookup::getCategoryForCurrentUser)
                .map(
                        category ->
                                GoalCategory.builder()
                                        .goal(goal)
                                        .category(category)
                                        .user(user)
                                        .build())
                .toList();
    }

    void updateGoalCategories(Goal goal, GoalRequest request, User user) {
        log.info("[updateGoalCategories]");

        goal.getGoalCategories().clear();
        goal.getGoalCategories().addAll(buildGoalCategories(request.categories(), goal, user));
    }

    void updateGoalWallets(Goal goal, GoalRequest request, User user) {
        log.info("[updateGoalWallets]");

        goal.getGoalWallets().clear();
        goal.getGoalWallets().addAll(buildGoalWallets(request.wallets(), goal, user));
    }

    void applyGoalUpdate(Goal goal, GoalRequest request, User user) {
        log.info("[applyGoalUpdate]");

        goalValidator.updateIfChanged(goal::getName, goal::setName, request.name());
        goalValidator.updateIfChanged(
                goal::getTargetAmount, goal::setTargetAmount, request.targetAmount());
        goalValidator.updateIfChanged(goal::getStartDate, goal::setStartDate, request.startDate());
        goalValidator.updateIfChanged(goal::getEndDate, goal::setEndDate, request.endDate());

        updateGoalWallets(goal, request, user);
        updateGoalCategories(goal, request, user);
    }

    Goal getGoalById(UUID id) {
        log.info("[getGoalById]={}", id);

        return goalRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND));
    }

    Goal getGoalForCurrentUser(UUID id) {
        log.info("[getGoalForCurrentUser]={}", id);

        Goal goal = getGoalById(id);

        securityHelper.checkIsOwner(goal);

        return goal;
    }
}
