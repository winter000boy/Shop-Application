package com.shopmanagement.repository;

import com.shopmanagement.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by ID and shopId (for multi-tenant isolation)
     */
    Optional<Product> findByIdAndShopId(Long id, Long shopId);
    
    /**
     * Find all products for a specific shop with pagination
     */
    Page<Product> findByShopId(Long shopId, Pageable pageable);
    
    /**
     * Find products by category for a specific shop
     */
    Page<Product> findByShopIdAndCategoryId(Long shopId, Long categoryId, Pageable pageable);
    
    /**
     * Find available products for a specific shop
     */
    Page<Product> findByShopIdAndAvailable(Long shopId, Boolean available, Pageable pageable);
    
    /**
     * Find products by category and availability
     */
    Page<Product> findByShopIdAndCategoryIdAndAvailable(Long shopId, Long categoryId, Boolean available, Pageable pageable);
    
    /**
     * Search products by name (case-insensitive, partial match)
     */
    Page<Product> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name, Pageable pageable);
    
    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE p.shopId = :shopId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("shopId") Long shopId, 
                                 @Param("searchTerm") String searchTerm, 
                                 Pageable pageable);
    
    /**
     * Count products by shop
     */
    Long countByShopId(Long shopId);
    
    /**
     * Count available products by shop
     */
    Long countByShopIdAndAvailable(Long shopId, Boolean available);
}
