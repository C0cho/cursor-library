package com.cc.library.service;

import com.cc.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
    Category getCategoryById(Long id);
    Category getCategoryByName(String name);
    Page<Category> getAllCategories(Pageable pageable);
    boolean existsByName(String name);
} 