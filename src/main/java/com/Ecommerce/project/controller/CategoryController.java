package com.Ecommerce.project.controller;

import com.Ecommerce.project.configuration.AppConstants;
import com.Ecommerce.project.payload.CategoryDTO;
import com.Ecommerce.project.payload.CategoryResponse;
import com.Ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("public/categories/")
    public ResponseEntity<CategoryResponse> getAllCategories(@RequestParam(name="pageNumber", defaultValue = AppConstants.CATEGORIES_PAGE_NUMBER, required = false) Integer pageNumber,
                                                             @RequestParam(name="pageSize", defaultValue = AppConstants.CATEGORIES_PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(name="sortBy", defaultValue = AppConstants.CATEGORIES_SORT_BY, required = false) String sortBy,
                                                             @RequestParam(name="sortDir", defaultValue = AppConstants.CATEGORIES_SORT_DIR, required = false) String sortDir) {
        CategoryResponse allCategories = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(allCategories, HttpStatus.OK);
    }

    @PostMapping("public/categories/createCategory")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO category) {
        CategoryDTO savedCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @PutMapping("public/categories/updateCategory/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                 @PathVariable Long categoryId) {
        CategoryDTO updatedDTO = categoryService.updateCategory(categoryId, categoryDTO);
        return new ResponseEntity(updatedDTO, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
        CategoryDTO deletedCategory = categoryService.deleteCategory(categoryId);
        return new ResponseEntity(deletedCategory, HttpStatus.OK);
    }
}
