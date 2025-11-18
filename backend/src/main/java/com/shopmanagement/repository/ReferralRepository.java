package com.shopmanagement.repository;

import com.shopmanagement.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    
    List<Referral> findByReferrerWalletId(Long walletId);
    
    Optional<Referral> findByReferredShopId(Long referredShopId);
    
    boolean existsByReferredShopId(Long referredShopId);
}
