package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.mapper.CategoryMapper;
import com.budgee.model.Category;
import com.budgee.model.User;
import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.request.CategoryUpdateRequest;
import com.budgee.payload.response.CategoryResponse;
import com.budgee.repository.CategoryRepository;
import com.budgee.service.CategoryService;
import com.budgee.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    UserService userService;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("[createCategory] {}", request.toString());

        Category newCategory = CategoryMapper.INSTANCE.toCategory(request);

        User authenticatedUser = userService.getCurrentUser();
        newCategory.setUser(authenticatedUser);

        log.warn("[categoryCategory] save to db");
        categoryRepository.save(newCategory);

        return CategoryMapper.INSTANCE.toCategoryResponse(newCategory);
    }

    @Override
    public CategoryResponse updateCategory(CategoryUpdateRequest request, UUID id) {
        log.info("[updateCategory]={}", request.toString());

        Category category = getCategoryById(id);
        User currentUserAuthenticated = userService.getCurrentUser();

        category.checkIsOwner(currentUserAuthenticated);

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

    private Category getCategoryById(UUID id) {
        log.info("[getCategoryById]={}", id);

        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
