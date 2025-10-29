package com.budgee.service.impl;

import com.budgee.service.validator.CategoryValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.budgee.enums.Role;
import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.CategoryMapper;
import com.budgee.model.*;
import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.request.CategoryUpdateRequest;
import com.budgee.payload.response.CategoryResponse;
import com.budgee.payload.response.PagedResponse;
import com.budgee.repository.CategoryRepository;
import com.budgee.repository.GoalCategoryRepository;
import com.budgee.repository.GoalRepository;
import com.budgee.repository.TransactionRepository;
import com.budgee.service.CategoryService;
import com.budgee.service.UserService;
import com.budgee.util.CommonHelper;
import com.budgee.util.SecurityHelper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    CategoryRepository categoryRepository;
    TransactionRepository transactionRepository;
    GoalCategoryRepository goalCategoryRepository;
    GoalRepository goalRepository;

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------
    CategoryMapper categoryMapper;

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------
    SecurityHelper securityHelper;

    // -------------------------------------------------------------------
    // VALIDATOR
    // -------------------------------------------------------------------
    CategoryValidator categoryValidator;

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    @Override
    public CategoryResponse getCategory(UUID id) {
        log.info("[getCategory]={}", id);

        Category category = getCategoryByIdForOwner(id);

        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("[createCategory] {}", request.toString());

        User authenticatedUser = securityHelper.getAuthenticatedUser();

        Category newCategory = categoryMapper.toCategory(request, authenticatedUser);

        // if the user is admin set category is default by system
        if (Role.ADMIN.equals(authenticatedUser.getRole())) {
            newCategory.setIsDefault(Boolean.TRUE);
        }

        log.warn("[categoryCategory] save to db");
        categoryRepository.save(newCategory);

        return categoryMapper.toCategoryResponse(newCategory);
    }

    @Override
    public CategoryResponse updateCategory(CategoryUpdateRequest request, UUID id) {
        log.info("[updateCategory]={}", request.toString());

        Category category = getCategoryByIdForOwner(id);

        applyCategoryUpdate(category, request);

        log.warn("[updateCategory] update to db");
        categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("[deleteCategory]={}", id);

        Category category = getCategoryByIdForOwner(id);

        deleteAssociatedTransactionsByCategory(category);

        removeCategoryFromGoal(category);

        log.warn("[deleteCategory] delete category from db");
        categoryRepository.delete(category);
    }

    @Override
    public PagedResponse<?> getAllCategoriesWithSortBy(int pageNo, int pageSize, String sortBy) {
        log.info(
                "[getAllCategoriesWithSortBy] page={} pageSize={} sortBy={}",
                pageNo,
                pageSize,
                sortBy);

        User authenticatedUser = securityHelper.getAuthenticatedUser();

        String SORT_BY = "(\\w+?)(:)(.*)";

        int page = 0;

        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            // name:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<CategoryResponse> categories =
                categoryRepository
                        .findAllByUser(authenticatedUser, pageable)
                        .map(categoryMapper::toCategoryResponse);

        return PagedResponse.fromPage(categories);
    }

    @Override
    public Category getCategoryByIdForOwner(UUID id) {
        log.info("[getCategoryByIdForOwner]={}", id);

        Category category = getCategoryById(id);
        securityHelper.checkIsOwner(category);

        return category;
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

    void applyCategoryUpdate(Category category, CategoryUpdateRequest request) {
        log.info("[applyCategoryUpdate]");

        categoryValidator.updateIfChanged(category::getName, category::setName, request.name());
        categoryValidator.updateIfChanged(category::getType, category::setType, request.type());
        categoryValidator.updateIfChanged(category::getIcon, category::setIcon, request.icon());
        categoryValidator.updateIfChanged(category::getColor, category::setColor, request.color());
    }

    @Transactional
    void removeCategoryFromGoal(Category category) {
        log.info("[removeCategoryFromGoal]={}", category.getId());

        goalCategoryRepository.deleteAllByCategory(category);

        User authenticatedUser = securityHelper.getAuthenticatedUser();

        List<GoalCategory> allGoalCategoriesOfUser =
                goalCategoryRepository.findAllByUser(authenticatedUser);

        List<UUID> categoriesIds =
                allGoalCategoriesOfUser.stream().map(x -> x.getCategory().getId()).toList();

        List<Goal> allGoalsOfAuthenticatedUserNotContainCategories =
                goalRepository.findGoalsNotContainingCategories(
                        authenticatedUser.getId(), categoriesIds);

        goalRepository.deleteAll(allGoalsOfAuthenticatedUserNotContainCategories);
    }

    @Transactional
    void deleteAssociatedTransactionsByCategory(Category category) {
        log.info(
                "[deleteAssociatedTransactionsByCategory] Deleting transactions for category={}",
                category.getId());

        User userAuthenticated = securityHelper.getAuthenticatedUser();

        transactionRepository.deleteAllByCategoryAndUser(category, userAuthenticated);
    }

    Category getCategoryById(UUID id) {
        log.info("[getCategoryById]={}", id);

        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
