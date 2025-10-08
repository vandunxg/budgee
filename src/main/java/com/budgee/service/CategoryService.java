package com.budgee.service;

import java.util.UUID;

import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.request.CategoryUpdateRequest;
import com.budgee.payload.response.CategoryResponse;
import com.budgee.payload.response.PagedResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(CategoryUpdateRequest request, UUID id);

    void deleteCategory(UUID id);

    PagedResponse<?> getAllCategoryWithSortBy(int page, int pageSize, String sortBy);
}
