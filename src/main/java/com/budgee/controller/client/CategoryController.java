package com.budgee.controller.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.CategoryRequest;
import com.budgee.payload.request.CategoryUpdateRequest;
import com.budgee.service.CategoryService;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

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
