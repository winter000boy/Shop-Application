package com.shopmanagement.repository;

import com.shopmanagement.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Find invoice by ID and shopId (for multi-tenant isolation)
     */
    Optional<Invoice> findByIdAndShopId(Long id, Long shopId);
    
    /**
     * Find all invoices for a specific shop with pagination
     */
    Page<Invoice> findByShopId(Long shopId, Pageable pageable);
    
    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumberAndShopId(String invoiceNumber, Long shopId);
    
    /**
     * Find invoice by repair order ID
     */
    Optional<Invoice> findByRepairOrderIdAndShopId(Long repairOrderId, Long shopId);
    
    /**
     * Find invoices by date range
     */
    @Query("SELECT i FROM Invoice i WHERE i.shopId = :shopId AND " +
           "i.createdAt BETWEEN :startDate AND :endDate")
    Page<Invoice> findByShopIdAndDateRange(@Param("shopId") Long shopId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);
    
    /**
     * Find sent invoices
     */
    Page<Invoice> findByShopIdAndSent(Long shopId, Boolean sent, Pageable pageable);
    
    /**
     * Count total invoices for a shop
     */
    Long countByShopId(Long shopId);
    
    /**
     * Get the latest invoice for generating new invoice numbers
     */
    @Query("SELECT i FROM Invoice i WHERE i.shopId = :shopId ORDER BY i.createdAt DESC")
    Page<Invoice> findLatestInvoiceByShopId(@Param("shopId") Long shopId, Pageable pageable);
}
