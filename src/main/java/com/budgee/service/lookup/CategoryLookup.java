package com.budgee.service.lookup;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.Category;
import com.budgee.repository.CategoryRepository;
import com.budgee.util.AuthContext;

@Component
@Slf4j(topic = "CATEGORY-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    CategoryRepository categoryRepository;

    // -------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------
    AuthContext authContext;

    public Category getCategoryById(UUID id) {
        log.info("[getCategoryById]={}", id);

        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    public Category getCategoryForCurrentUser(UUID categoryId) {
        log.info("[getCategoryForCurrentUser] categoryId={}", categoryId);

        Category category = this.getCategoryById(categoryId);
        authContext.checkIsOwner(category);

        return category;
    }
}
