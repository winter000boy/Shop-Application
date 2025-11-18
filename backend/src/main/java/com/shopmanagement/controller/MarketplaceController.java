package com.shopmanagement.controller;

import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.CategoryResponse;
import com.shopmanagement.dto.response.ProductResponse;
import com.shopmanagement.entity.Category;
import com.shopmanagement.entity.Product;
import com.shopmanagement.service.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {
    
    private final MarketplaceService marketplaceService;
    
    /**
     * Browse marketplace products with pagination and optional filters
     */
    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getMarketplaceProducts(
            @RequestParam(required = false) Long categoryId,
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
            products = marketplaceService.searchMarketplaceProducts(search, pageable);
        } else if (categoryId != null) {
            products = marketplaceService.getMarketplaceProductsByCategory(categoryId, pageable);
        } else {
            products = marketplaceService.getAllMarketplaceProducts(pageable);
        }
        
        Page<ProductResponse> response = products.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all categories
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<Category> categories = marketplaceService.getAllCategories();
        List<CategoryResponse> response = categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get nearby wholesalers sorted by distance
     */
    @GetMapping("/wholesalers")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<MarketplaceService.WholesalerInfo>>> getNearbyWholesalers(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer maxDistance) {
        
        List<MarketplaceService.WholesalerInfo> wholesalers = 
                marketplaceService.getNearbyWholesalers(latitude, longitude, maxDistance);
        
        return ResponseEntity.ok(ApiResponse.success(wholesalers));
    }
    
    /**
     * Search marketplace products
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchMarketplace(
            @RequestParam String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Product> products;
        if (categoryId != null) {
            // Search within category
            products = marketplaceService.searchMarketplaceProducts(query, pageable);
        } else {
            products = marketplaceService.searchMarketplaceProducts(query, pageable);
        }
        
        Page<ProductResponse> response = products.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // Helper methods for DTO conversion
    
    private ProductResponse convertToResponse(Product product) {
        ProductResponse.CategorySummary categorySummary = ProductResponse.CategorySummary.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .description(product.getCategory().getDescription())
                .build();
        
        // Note: Shop information would need to be fetched from Shop entity
        // For now, we'll leave it null or fetch it if needed
        ProductResponse.ShopSummary shopSummary = ProductResponse.ShopSummary.builder()
                .id(product.getShopId())
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
                .shop(shopSummary)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount((long) category.getProducts().size())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
