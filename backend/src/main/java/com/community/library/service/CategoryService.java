package com.community.library.service;

import com.community.library.entity.Category;
import com.community.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    @Transactional
    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        category.setName(name);
        category.setDescription(description);
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("分类不存在");
        }
        categoryRepository.deleteById(id);
    }
}
