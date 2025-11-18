package com.shopmanagement.service;

import com.shopmanagement.entity.Category;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    // Predefined categories for mobile and hardware repair shops
    private static final List<String[]> INITIAL_CATEGORIES = Arrays.asList(
            new String[]{"Battery", "Mobile phone batteries, power banks, and battery accessories"},
            new String[]{"Camera", "Camera modules, lenses, and camera replacement parts"},
            new String[]{"Speakers", "Earpiece speakers, loudspeakers, and audio components"},
            new String[]{"ICs", "Integrated circuits, chips, and microcontrollers"},
            new String[]{"Display", "LCD screens, OLED displays, and touch digitizers"},
            new String[]{"Charging Port", "USB ports, charging connectors, and flex cables"},
            new String[]{"Motherboard", "Logic boards, mainboards, and PCB components"},
            new String[]{"Tools", "Repair tools, screwdrivers, and opening tools"},
            new String[]{"Adhesives", "Screen adhesives, glue, and bonding materials"},
            new String[]{"Protective", "Screen protectors, cases, and protective accessories"}
    );
    
    /**
     * Initialize categories on application startup
     */
    @PostConstruct
    @Transactional
    public void initializeCategories() {
        log.info("Initializing product categories...");
        
        for (String[] categoryData : INITIAL_CATEGORIES) {
            String name = categoryData[0];
            String description = categoryData[1];
            
            if (!categoryRepository.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(description);
                categoryRepository.save(category);
                log.info("Created category: {}", name);
            }
        }
        
        log.info("Category initialization complete");
    }
    
    /**
     * Get all categories with pagination
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    /**
     * Get all categories without pagination
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }
    
    /**
     * Get category by name
     */
    @Transactional(readOnly = true)
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }
    
    /**
     * Create a new category
     */
    @Transactional
    public Category createCategory(Category category) {
        log.info("Creating category: {}", category.getName());
        return categoryRepository.save(category);
    }
    
    /**
     * Update an existing category
     */
    @Transactional
    public Category updateCategory(Long categoryId, Category categoryUpdate) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        existingCategory.setName(categoryUpdate.getName());
        existingCategory.setDescription(categoryUpdate.getDescription());
        
        log.info("Updating category: {}", categoryId);
        return categoryRepository.save(existingCategory);
    }
    
    /**
     * Delete a category
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        log.info("Deleting category: {}", categoryId);
        categoryRepository.delete(category);
    }
}
