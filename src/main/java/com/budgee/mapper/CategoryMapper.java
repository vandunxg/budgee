package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Category;
import com.budgee.model.User;
import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.response.CategoryResponse;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "user", source = "authenticatedUser")
    Category toCategory(CategoryRequest request, User authenticatedUser);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "editable", expression = "java( !category.getIsDefault() )")
    @Mapping(target = "deletable", expression = "java( !category.getIsDefault() )")
    CategoryResponse toCategoryResponse(Category category);
}
