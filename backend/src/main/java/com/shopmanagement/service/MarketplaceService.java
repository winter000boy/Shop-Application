package com.shopmanagement.service;

import com.shopmanagement.entity.Category;
import com.shopmanagement.entity.Product;
import com.shopmanagement.repository.CategoryRepository;
import com.shopmanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Get all marketplace products (from all shops) with pagination
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllMarketplaceProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    /**
     * Get marketplace products filtered by category
     */
    @Transactional(readOnly = true)
    public Page<Product> getMarketplaceProductsByCategory(Long categoryId, Pageable pageable) {
        // Get products from all shops for this category
        return productRepository.findAll(pageable).map(product -> {
            if (product.getCategory().getId().equals(categoryId)) {
                return product;
            }
            return null;
        }).map(p -> p);
    }
    
    /**
     * Search marketplace products by name or description
     */
    @Transactional(readOnly = true)
    public Page<Product> searchMarketplaceProducts(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        
        // Search across all shops
        List<Product> allProducts = productRepository.findAll();
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        List<Product> filteredProducts = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerSearchTerm) ||
                           (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerSearchTerm)))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());
        
        List<Product> pageContent = filteredProducts.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredProducts.size());
    }
    
    /**
     * Get all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Get nearby wholesalers with dummy distance-based data
     */
    public List<WholesalerInfo> getNearbyWholesalers(Double latitude, Double longitude, Integer maxDistance) {
        // Generate dummy wholesaler data
        List<WholesalerInfo> wholesalers = generateDummyWholesalers();
        
        // If coordinates provided, calculate distances and filter
        if (latitude != null && longitude != null) {
            wholesalers.forEach(w -> {
                double distance = calculateDistance(latitude, longitude, w.getLatitude(), w.getLongitude());
                w.setDistance(distance);
            });
            
            // Filter by max distance if provided
            if (maxDistance != null) {
                wholesalers = wholesalers.stream()
                        .filter(w -> w.getDistance() <= maxDistance)
                        .collect(Collectors.toList());
            }
            
            // Sort by distance
            wholesalers.sort(Comparator.comparingDouble(WholesalerInfo::getDistance));
        }
        
        return wholesalers;
    }
    
    /**
     * Generate dummy wholesaler data
     */
    private List<WholesalerInfo> generateDummyWholesalers() {
        List<WholesalerInfo> wholesalers = new ArrayList<>();
        
        wholesalers.add(WholesalerInfo.builder()
                .id(1L)
                .name("Mobile Parts Hub")
                .phoneNumber("+1-555-0101")
                .email("contact@mobilepartshub.com")
                .address("123 Tech Street, Silicon Valley, CA 94025")
                .latitude(37.4419)
                .longitude(-122.1430)
                .distance(0.0)
                .rating(4.5)
                .specialties(Arrays.asList("Display", "Battery", "Camera"))
                .build());
        
        wholesalers.add(WholesalerInfo.builder()
                .id(2L)
                .name("Tech Components Wholesale")
                .phoneNumber("+1-555-0102")
                .email("sales@techcomponents.com")
                .address("456 Hardware Ave, San Jose, CA 95110")
                .latitude(37.3382)
                .longitude(-121.8863)
                .distance(0.0)
                .rating(4.7)
                .specialties(Arrays.asList("ICs", "Motherboard", "Charging Port"))
                .build());
        
        wholesalers.add(WholesalerInfo.builder()
                .id(3L)
                .name("Repair Tools Direct")
                .phoneNumber("+1-555-0103")
                .email("info@repairtoolsdirect.com")
                .address("789 Industrial Blvd, Fremont, CA 94538")
                .latitude(37.5485)
                .longitude(-121.9886)
                .distance(0.0)
                .rating(4.3)
                .specialties(Arrays.asList("Tools", "Adhesives", "Protective"))
                .build());
        
        wholesalers.add(WholesalerInfo.builder()
                .id(4L)
                .name("Premium Mobile Parts")
                .phoneNumber("+1-555-0104")
                .email("orders@premiummobileparts.com")
                .address("321 Commerce Way, Oakland, CA 94607")
                .latitude(37.8044)
                .longitude(-122.2712)
                .distance(0.0)
                .rating(4.8)
                .specialties(Arrays.asList("Display", "Speakers", "Camera"))
                .build());
        
        wholesalers.add(WholesalerInfo.builder()
                .id(5L)
                .name("Electronics Wholesale Center")
                .phoneNumber("+1-555-0105")
                .email("support@electronicswholesale.com")
                .address("555 Market Street, San Francisco, CA 94105")
                .latitude(37.7749)
                .longitude(-122.4194)
                .distance(0.0)
                .rating(4.6)
                .specialties(Arrays.asList("Battery", "ICs", "Motherboard"))
                .build());
        
        return wholesalers;
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Wholesaler information DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WholesalerInfo {
        private Long id;
        private String name;
        private String phoneNumber;
        private String email;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double distance; // in kilometers
        private Double rating;
        private List<String> specialties;
    }
}
