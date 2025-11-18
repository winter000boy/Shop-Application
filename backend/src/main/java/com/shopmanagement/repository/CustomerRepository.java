package com.shopmanagement.repository;

import com.shopmanagement.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find customer by ID and shopId (for multi-tenant isolation)
     */
    Optional<Customer> findByIdAndShopId(Long id, Long shopId);
    
    /**
     * Find all customers for a specific shop with pagination
     */
    Page<Customer> findByShopId(Long shopId, Pageable pageable);
    
    /**
     * Search customers by name (case-insensitive, partial match)
     */
    Page<Customer> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name, Pageable pageable);
    
    /**
     * Search customers by phone number (exact match)
     */
    Page<Customer> findByShopIdAndPhoneNumber(Long shopId, String phoneNumber, Pageable pageable);
    
    /**
     * Search customers by email (case-insensitive, partial match)
     */
    Page<Customer> findByShopIdAndEmailContainingIgnoreCase(Long shopId, String email, Pageable pageable);
    
    /**
     * Search customers by multiple criteria (name, phone, or email)
     */
    @Query("SELECT c FROM Customer c WHERE c.shopId = :shopId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> searchCustomers(@Param("shopId") Long shopId, 
                                   @Param("searchTerm") String searchTerm, 
                                   Pageable pageable);
    
    /**
     * Check if customer exists by phone number in a shop
     */
    Boolean existsByShopIdAndPhoneNumber(Long shopId, String phoneNumber);
    
    /**
     * Check if customer exists by email in a shop
     */
    Boolean existsByShopIdAndEmail(Long shopId, String email);
}
