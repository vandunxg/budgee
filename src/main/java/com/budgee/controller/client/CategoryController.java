package com.budgee.controller.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.request.CategoryUpdateRequest;
import com.budgee.service.CategoryService;
import com.budgee.util.MessageConstants;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    @RequestMapping("/{id}")
    ResponseEntity<?> get(@PathVariable UUID id) {
        log.info("[GET /categories/{}]", id);

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, categoryService.getCategory(id));
    }

    @PostMapping("/")
    ResponseEntity<?> create(@Valid @RequestBody CategoryRequest request) {
        log.info("[POST /categories/] {}", request.toString());

        return ResponseUtil.created(categoryService.createCategory(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable UUID id, @RequestBody CategoryUpdateRequest request) {
        log.info("[PATCH /categories/{}]={}", id, request.toString());

        return ResponseUtil.success(
                "Updated successfully", categoryService.updateCategory(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        log.info("[DELETE /categories/{}]", id);

        categoryService.deleteCategory(id);

        return ResponseUtil.deleted();
    }

    @GetMapping("/list")
    ResponseEntity<?> list(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String sortBy) {
        log.info("[GET /categories/list/pageNo={}&pageSize={}&sortBy={}]", pageNo, pageSize, sortBy);

        return ResponseUtil.success(
                MessageConstants.FETCH_SUCCESS,
                categoryService.getAllCategoriesWithSortBy(pageNo, pageSize, sortBy));
    }
}
