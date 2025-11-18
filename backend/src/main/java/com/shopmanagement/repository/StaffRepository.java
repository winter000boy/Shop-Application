package com.shopmanagement.repository;

import com.shopmanagement.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    
    /**
     * Find staff by ID and shopId (for multi-tenant isolation)
     */
    Optional<Staff> findByIdAndShopId(Long id, Long shopId);
    
    /**
     * Find all staff for a specific shop with pagination
     */
    Page<Staff> findByShopId(Long shopId, Pageable pageable);
    
    /**
     * Find active staff for a specific shop
     */
    Page<Staff> findByShopIdAndActive(Long shopId, Boolean active, Pageable pageable);
}
