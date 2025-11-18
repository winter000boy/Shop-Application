package com.shopmanagement.repository;

import com.shopmanagement.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a specific shop with pagination
     */
    Page<Notification> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    
    /**
     * Find unread notifications for a specific shop
     */
    Page<Notification> findByShopIdAndIsReadOrderByCreatedAtDesc(Long shopId, Boolean isRead, Pageable pageable);
    
    /**
     * Count unread notifications for a shop
     */
    Long countByShopIdAndIsRead(Long shopId, Boolean isRead);
}
