package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Category;
import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.response.CategoryResponse;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "name", source = "request.name")
    Category toCategory(CategoryRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    CategoryResponse toCategoryResponse(Category category);
}
