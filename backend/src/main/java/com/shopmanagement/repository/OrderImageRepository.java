package com.shopmanagement.repository;

import com.shopmanagement.entity.OrderImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderImageRepository extends JpaRepository<OrderImage, Long> {
    
    /**
     * Find all images for a specific repair order
     */
    List<OrderImage> findByOrderId(Long orderId);
    
    /**
     * Delete all images for a specific repair order
     */
    void deleteByOrderId(Long orderId);
}
