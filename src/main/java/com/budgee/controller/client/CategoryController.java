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

    @GetMapping("/list")
    ResponseEntity<?> getAllCategoryWithSortBy(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String sortBy) {
        log.info("[GET /list/pageNo={}&pageSize={}&sortBy={}]", pageNo, pageSize, sortBy);

        return ResponseUtil.success(
                MessageConstants.FETCH_SUCCESS,
                categoryService.getAllCategoryWithSortBy(pageNo, pageSize, sortBy));
    }

    @PostMapping("/")
    ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("[POST /category/] {}", request.toString());

        return ResponseUtil.created(categoryService.createCategory(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable UUID id, @RequestBody CategoryUpdateRequest request) {
        log.info("[PATCH /category/{}]={}", id, request.toString());

        return ResponseUtil.success(
                "Updated successfully", categoryService.updateCategory(request, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable UUID id) {
        log.info("[DELETE /category/{}]", id);

        categoryService.deleteCategory(id);

        return ResponseUtil.deleted();
    }
}
