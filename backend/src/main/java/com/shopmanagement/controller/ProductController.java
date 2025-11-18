package com.shopmanagement.controller;

import com.shopmanagement.dto.request.ProductRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.ProductResponse;
import com.shopmanagement.entity.Category;
import com.shopmanagement.entity.Product;
import com.shopmanagement.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Get all products with pagination and optional filters
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products;
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else if (categoryId != null && available != null) {
            products = productService.getProductsByCategoryAndAvailability(categoryId, available, pageable);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId, pageable);
        } else if (available != null) {
            products = productService.getProductsByAvailability(available, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }
        
        Page<ProductResponse> response = products.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(product)));
    }
    
    /**
     * Create a new product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = convertToEntity(request);
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(convertToResponse(createdProduct), "Product created successfully"));
    }
    
    /**
     * Update an existing product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        
        Product product = convertToEntity(request);
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(updatedProduct), "Product updated successfully"));
    }
    
    /**
     * Delete a product
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
    
    /**
     * Upload images for a product
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        
        Product product = productService.uploadProductImages(id, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(convertToResponse(product), "Images uploaded successfully"));
    }
    
    /**
     * Delete a product image
     */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProductImage(
            @PathVariable Long id,
            @RequestParam String imageUrl) {
        
        Product product = productService.deleteProductImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(product), "Image deleted successfully"));
    }
    
    /**
     * Get product count
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Long>> getProductCount() {
        Long count = productService.getProductCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * Get available product count
     */
    @GetMapping("/count/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Long>> getAvailableProductCount() {
        Long count = productService.getAvailableProductCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    // Helper methods for DTO conversion
    
    private ProductResponse convertToResponse(Product product) {
        ProductResponse.CategorySummary categorySummary = ProductResponse.CategorySummary.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .description(product.getCategory().getDescription())
                .build();
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(categorySummary)
                .imageUrls(product.getImageUrls())
                .stockQuantity(product.getStockQuantity())
                .available(product.getAvailable())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    private Product convertToEntity(ProductRequest request) {
        Product product = new Product();
        
        // Set category (will be validated in service)
        Category category = new Category();
        category.setId(request.getCategoryId());
        product.setCategory(category);
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setAvailable(request.getAvailable());
        
        return product;
    }
}
