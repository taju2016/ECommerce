package com.Ecommerce.project.service.Impl;

import com.Ecommerce.project.exceptions.APIException;
import com.Ecommerce.project.exceptions.ResourceNotFoundException;
import com.Ecommerce.project.model.Category;
import com.Ecommerce.project.payload.CategoryDTO;
import com.Ecommerce.project.payload.CategoryResponse;
import com.Ecommerce.project.repositories.CategoryRepository;
import com.Ecommerce.project.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        List<Category> categories = categoryPage.getContent();
        if(categories == null || categories.size() == 0) throw new APIException("There are no Categories present");
        List<CategoryDTO> categoryDTOS =  categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        CategoryResponse response = new CategoryResponse();
        response.setContent(categoryDTOS);
        response.setPageNumber(categoryPage.getNumber());
        response.setPageSize(categoryPage.getSize());
        response.setTotalPages(categoryPage.getTotalPages());
        response.setTotalElements(categoryPage.getTotalElements());
        response.setIsLastPage(categoryPage.isLast());
        return response;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category existing = categoryRepository.findByCategoryName(category.getCategoryName());
        if(existing != null) throw new APIException("Category with the name: " + category.getCategoryName() + " already exists");
        Category savedCategory = (Category)categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category requested = modelMapper.map(categoryDTO, Category.class);
        Optional<Category> categories = categoryRepository.findById(categoryId);
        Category updateCategory = categories
                .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryID", requested.getCategoryId()));
        updateCategory.setCategoryName(requested.getCategoryName());
        Category updated = categoryRepository.save(updateCategory);
        CategoryDTO updatedDTO = modelMapper.map(updated, CategoryDTO.class);
        return updatedDTO;

    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Optional<Category> categories = categoryRepository.findById(categoryId);
        Category deleteCategory = categories
                .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        categoryRepository.delete(deleteCategory);
        CategoryDTO deletedCategory = modelMapper.map(categories.get(), CategoryDTO.class);
        return deletedCategory;
    }


}
