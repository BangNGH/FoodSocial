package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.Category;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService{
    @Autowired
    private CategoryRepository categoryRepository;
    public List<Category> getList() {
        return categoryRepository.findAll();
    }

    public void delete(Long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isPresent()){
            categoryRepository.deleteById(id);
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }

    public Optional get(Long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isPresent()){
            return result;
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }
    public void addCategory(Category category){
        categoryRepository.save(category);
    }
    public void updateCategory(Category category){
        Category existingCategory = categoryRepository.findById(category.getId()).orElse(null);
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        categoryRepository.save(existingCategory);
    }
}
