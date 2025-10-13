package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.budgee.model.Category;
import com.budgee.model.User;
import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.request.CategoryUpdateRequest;
import com.budgee.payload.response.CategoryResponse;
import com.budgee.payload.response.PagedResponse;
import com.budgee.repository.CategoryRepository;
import com.budgee.service.CategoryService;
import com.budgee.service.UserService;
import com.budgee.util.Helpers;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    UserService userService;
    Helpers helpers;

    @Override
    public CategoryResponse getCategory(UUID id) {
        log.info("[getCategory]={}", id);

        Category category = getCategoryByIdForOwner(id);

        return CategoryMapper.INSTANCE.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("[createCategory] {}", request.toString());

        Category newCategory = CategoryMapper.INSTANCE.toCategory(request);

        User authenticatedUser = userService.getCurrentUser();
        newCategory.setUser(authenticatedUser);

        // if the user is admin set category is default by system
        if (Role.ADMIN.equals(authenticatedUser.getRole())) {
            newCategory.setIsDefault(Boolean.TRUE);
        }

        log.warn("[categoryCategory] save to db");
        categoryRepository.save(newCategory);

        return CategoryMapper.INSTANCE.toCategoryResponse(newCategory);
    }

    @Override
    public CategoryResponse updateCategory(CategoryUpdateRequest request, UUID id) {
        log.info("[updateCategory]={}", request.toString());

        Category category = getCategoryByIdForOwner(id);

        String currentCategoryName = category.getName();

        if (StringUtils.hasText(request.name()) && !currentCategoryName.equals(request.name())) {
            category.setName(request.name());
        }

        String currentCategoryColor = category.getColor();

        if (StringUtils.hasText(request.color()) && !currentCategoryColor.equals(request.color())) {
            category.setColor(request.color());
        }

        String currentCategoryIcon = category.getColor();

        if (StringUtils.hasText(request.icon()) && !currentCategoryIcon.equals(request.icon())) {
            category.setIcon(request.icon());
        }

        log.warn("[updateCategory] update to db");
        categoryRepository.save(category);

        return CategoryMapper.INSTANCE.toCategoryResponse(category);
    }

    @Override
    public void deleteCategory(UUID id) {
        log.info("[deleteCategory]={}", id);

        Category category = getCategoryByIdForOwner(id);

        deleteAssociatedTransactions(category);

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

        User authenticatedUser = userService.getCurrentUser();

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
                        .map(CategoryMapper.INSTANCE::toCategoryResponse);

        return PagedResponse.fromPage(categories);
    }

    @Override
    public Category getCategoryByIdForOwner(UUID id) {
        log.info("[getCategoryByIdForOwner]={}", id);

        Category category = getCategoryById(id);
        helpers.checkIsOwner(category);

        return category;
    }

    // PRIVATE FUNCTION

    void deleteAssociatedTransactions(Category category) {
        log.info(
                "[deleteAssociatedTransactions] Deleting transactions for category={}",
                category.getId());
    }

    Category getCategoryById(UUID id) {
        log.info("[getCategoryById]={}", id);

        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
