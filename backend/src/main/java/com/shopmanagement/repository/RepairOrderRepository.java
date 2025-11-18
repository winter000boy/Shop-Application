package com.shopmanagement.repository;

import com.shopmanagement.entity.OrderStatus;
import com.shopmanagement.entity.RepairOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    
    /**
     * Find repair order by ID and shopId (for multi-tenant isolation)
     */
    Optional<RepairOrder> findByIdAndShopId(Long id, Long shopId);
    
    /**
     * Find all repair orders for a specific shop with pagination
     */
    Page<RepairOrder> findByShopId(Long shopId, Pageable pageable);
    
    /**
     * Find repair orders by status
     */
    Page<RepairOrder> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);
    
    /**
     * Find repair orders by customer
     */
    Page<RepairOrder> findByShopIdAndCustomerId(Long shopId, Long customerId, Pageable pageable);
    
    /**
     * Find repair orders by assigned staff
     */
    Page<RepairOrder> findByShopIdAndAssignedStaffId(Long shopId, Long staffId, Pageable pageable);
    
    /**
     * Find repair orders by date range
     */
    @Query("SELECT ro FROM RepairOrder ro WHERE ro.shopId = :shopId AND " +
           "ro.repairDate BETWEEN :startDate AND :endDate")
    Page<RepairOrder> findByShopIdAndDateRange(@Param("shopId") Long shopId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);
    
    /**
     * Find repair orders by status and date range
     */
    @Query("SELECT ro FROM RepairOrder ro WHERE ro.shopId = :shopId AND " +
           "ro.status = :status AND ro.repairDate BETWEEN :startDate AND :endDate")
    Page<RepairOrder> findByShopIdAndStatusAndDateRange(@Param("shopId") Long shopId,
                                                         @Param("status") OrderStatus status,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate,
                                                         Pageable pageable);
    
    /**
     * Search repair orders by device model or serial number
     */
    @Query("SELECT ro FROM RepairOrder ro WHERE ro.shopId = :shopId AND " +
           "(LOWER(ro.deviceModel) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ro.serialNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<RepairOrder> searchOrders(@Param("shopId") Long shopId,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);
    
    /**
     * Count orders by status for a shop
     */
    Long countByShopIdAndStatus(Long shopId, OrderStatus status);
    
    /**
     * Get all orders for a customer (for customer history)
     */
    List<RepairOrder> findByShopIdAndCustomerIdOrderByCreatedAtDesc(Long shopId, Long customerId);
}
