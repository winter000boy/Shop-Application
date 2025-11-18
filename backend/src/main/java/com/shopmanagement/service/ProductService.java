package com.shopmanagement.service;

import com.shopmanagement.entity.Category;
import com.shopmanagement.entity.Product;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.ValidationException;
import com.shopmanagement.repository.CategoryRepository;
import com.shopmanagement.repository.ProductRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;
    
    /**
     * Get all products for the current shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return productRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Get products filtered by category
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        return productRepository.findByShopIdAndCategoryId(shopId, categoryId, pageable);
    }
    
    /**
     * Get products filtered by availability
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductsByAvailability(Boolean available, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return productRepository.findByShopIdAndAvailable(shopId, available, pageable);
    }
    
    /**
     * Get products filtered by category and availability
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryAndAvailability(Long categoryId, Boolean available, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        return productRepository.findByShopIdAndCategoryIdAndAvailable(shopId, categoryId, available, pageable);
    }
    
    /**
     * Search products by name or description
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return productRepository.findByShopId(shopId, pageable);
        }
        
        return productRepository.searchProducts(shopId, searchTerm.trim(), pageable);
    }
    
    /**
     * Get product by ID (with shop-level isolation)
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        Long shopId = ShopContext.getCurrentShopId();
        return productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }
    
    /**
     * Create a new product
     */
    @Transactional
    public Product createProduct(Product product) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Validate category exists
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + product.getCategory().getId()));
        
        // Set shopId for multi-tenant isolation
        product.setShopId(shopId);
        product.setCategory(category);
        
        // Initialize image URLs list if null
        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }
        
        // Set default values
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getAvailable() == null) {
            product.setAvailable(true);
        }
        
        log.info("Creating product: {} in shop: {}", product.getName(), shopId);
        return productRepository.save(product);
    }
    
    /**
     * Update an existing product
     */
    @Transactional
    public Product updateProduct(Long productId, Product productUpdate) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Product existingProduct = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Validate category if changed
        if (productUpdate.getCategory() != null && productUpdate.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productUpdate.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + productUpdate.getCategory().getId()));
            existingProduct.setCategory(category);
        }
        
        // Update fields
        existingProduct.setName(productUpdate.getName());
        existingProduct.setDescription(productUpdate.getDescription());
        existingProduct.setPrice(productUpdate.getPrice());
        existingProduct.setStockQuantity(productUpdate.getStockQuantity());
        existingProduct.setAvailable(productUpdate.getAvailable());
        
        log.info("Updating product: {} in shop: {}", productId, shopId);
        return productRepository.save(existingProduct);
    }
    
    /**
     * Delete a product
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Product product = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Delete associated images from storage
        for (String imageUrl : product.getImageUrls()) {
            try {
                storageService.deleteFile(imageUrl);
            } catch (Exception e) {
                log.error("Failed to delete image: {}", imageUrl, e);
            }
        }
        
        log.info("Deleting product: {} in shop: {}", productId, shopId);
        productRepository.delete(product);
    }
    
    /**
     * Upload images for a product
     */
    @Transactional
    public Product uploadProductImages(Long productId, List<MultipartFile> files) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Product product = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
            product.setImageUrls(imageUrls);
        }
        
        for (MultipartFile file : files) {
            try {
                // Upload file to storage
                String imageUrl = storageService.uploadFile(file, "products/" + productId);
                imageUrls.add(imageUrl);
                
                log.info("Uploaded image for product {}: {}", productId, imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload image for product: {}", productId, e);
                throw new ValidationException("Failed to upload image: " + e.getMessage());
            }
        }
        
        return productRepository.save(product);
    }
    
    /**
     * Delete a product image
     */
    @Transactional
    public Product deleteProductImage(Long productId, String imageUrl) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Product product = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls == null || !imageUrls.contains(imageUrl)) {
            throw new ValidationException("Image not found in product");
        }
        
        // Remove from list
        imageUrls.remove(imageUrl);
        
        // Delete from storage
        try {
            storageService.deleteFile(imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete image from storage: {}", imageUrl, e);
        }
        
        log.info("Deleted image for product {}: {}", productId, imageUrl);
        return productRepository.save(product);
    }
    
    /**
     * Get product count for current shop
     */
    @Transactional(readOnly = true)
    public Long getProductCount() {
        Long shopId = ShopContext.getCurrentShopId();
        return productRepository.countByShopId(shopId);
    }
    
    /**
     * Get available product count for current shop
     */
    @Transactional(readOnly = true)
    public Long getAvailableProductCount() {
        Long shopId = ShopContext.getCurrentShopId();
        return productRepository.countByShopIdAndAvailable(shopId, true);
    }
}
