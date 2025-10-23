package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
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
    // HELPER
    // -------------------------------------------------------------------
    GoalMapper goalMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    DateValidator dateValidator;
    SecurityHelper securityHelper;
    WalletHelper walletHelper;

    @Override
    public GoalResponse createGoal(GoalRequest request) {
        log.info("[createGoal]={}", request);

        User authenticatedUser = userService.getCurrentUser();

        List<Wallet> wallets = getListWalletsById(request.wallets());

        List<Category> categories = getListCategoriesById(request.categories());

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());

        Goal goal = goalMapper.toGoal(request);

        goal.setGoalCategories(
                categories.stream()
                        .map(
                                x ->
                                        GoalCategory.builder()
                                                .goal(goal)
                                                .category(x)
                                                .user(authenticatedUser)
                                                .build())
                        .toList());

        goal.setGoalWallets(
                wallets.stream()
                        .map(
                                x ->
                                        GoalWallet.builder()
                                                .goal(goal)
                                                .wallet(x)
                                                .user(authenticatedUser)
                                                .build())
                        .toList());

        goal.setUser(authenticatedUser);
        goal.setCurrentAmount(BigDecimal.ZERO);

        log.warn("[createGoal] save to db");
        goalRepository.save(goal);

        return toGoalResponse(goal);
    }

    @Override
    public GoalResponse getGoal(UUID id) {
        log.info("[getGoal]={}", id);

        Goal goal = getGoalById(id);
        securityHelper.checkIsOwner(goal);

        return toGoalResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID id, GoalRequest request) {
        log.info("[updateGoal] id={} request={}", id, request);

        Goal goal = getGoalOfCurrentUserById(id);

        User authenticatedUser = userService.getCurrentUser();

        List<Category> categories =
                request.categories().stream()
                        .map(categoryService::getCategoryByIdForOwner)
                        .toList();

        goal.getGoalCategories().clear();

        categories.forEach(
                x -> {
                    GoalCategory goalCategory = createGoalCategory(goal, x, authenticatedUser);

                    goal.getGoalCategories().add(goalCategory);
                });

        List<Wallet> wallets =
                request.wallets().stream().map(walletHelper::getWalletByIdForOwner).toList();

        goal.getGoalWallets().clear();

        wallets.forEach(
                x -> {
                    GoalWallet goalWallet = createGoalWallet(goal, x, authenticatedUser);

                    goal.getGoalWallets().add(goalWallet);
                });

        if (!goal.getName().equals(request.name())) {
            goal.setName(request.name());
        }

        if (goal.getTargetAmount().compareTo(request.targetAmount()) != 0) {
            goal.setTargetAmount(request.targetAmount());
        }

        dateValidator.checkEndDateBeforeStartDate(request.startDate(), request.endDate());

        goal.setStartDate(request.startDate());
        goal.setEndDate(request.endDate());

        return toGoalResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(UUID id) {
        log.info("[deleteGoal] id={}", id);

        User authenticatedUser = userService.getCurrentUser();

        Goal goal = getGoalById(id);
        goal.checkIsOwner(authenticatedUser);

        goal.getGoalWallets().clear();
        goal.getGoalCategories().clear();

        log.warn("[deleteGoal] deleting from db");
        goalRepository.delete(goal);
    }

    @Override
    public List<GoalResponse> getListGoals() {
        log.info("[getListGoals]");

        List<Goal> goals = getAllGoalsByUser();

        return goals.stream().map(this::toGoalResponse).toList();
    }

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------

    List<Goal> getAllGoalsByUser() {
        log.info("[getAllGoalsByUser]");

        User authenticatedUser = userService.getCurrentUser();

        return goalRepository.findAllByUser(authenticatedUser);
    }

    GoalWallet createGoalWallet(Goal goal, Wallet wallet, User user) {
        log.info("[createGoalWallet]");

        return GoalWallet.builder().wallet(wallet).goal(goal).user(user).build();
    }

    GoalCategory createGoalCategory(Goal goal, Category category, User user) {
        log.info("[createGoalCategory]");

        return GoalCategory.builder().category(category).user(user).goal(goal).build();
    }

    Goal getGoalOfCurrentUserById(UUID id) {
        log.info("[getGoalOfCurrentUserById] id={}", id);

        User authenticatedUser = userService.getCurrentUser();

        Goal goal = getGoalById(id);
        goal.checkIsOwner(authenticatedUser);

        return goal;
    }

    Goal getGoalById(UUID id) {
        log.info("[getGoalById]={}", id);

        return goalRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND));
    }

    GoalResponse toGoalResponse(Goal goal) {
        log.info("[toGoalResponse]={}", goal);

        GoalResponse response = goalMapper.toGoalResponse(goal);

        response.setCategoriesId(
                goal.getGoalCategories().stream().map(x -> x.getCategory().getId()).toList());

        response.setWalletsId(
                goal.getGoalWallets().stream().map(x -> x.getWallet().getId()).toList());

        return response;
    }

    List<Wallet> getListWalletsById(List<UUID> walletsId) {
        log.info("[getListWalletsById]={}", walletsId.toString());

        List<Wallet> wallets = walletsId.stream().map(walletHelper::getWalletByIdForOwner).toList();

        if (wallets.isEmpty()) {
            throw new ValidationException(ErrorCode.WALLET_IS_REQUIRED);
        }

        return wallets;
    }

    List<Category> getListCategoriesById(List<UUID> categoriesId) {
        log.info("[getListCategoriesById]={}", categoriesId.toString());

        List<Category> categories =
                categoriesId.stream().map(categoryService::getCategoryByIdForOwner).toList();

        if (categories.isEmpty()) {
            throw new ValidationException(ErrorCode.CATEGORY_IS_REQUIRED);
        }

        return categories;
    }
}
