package com.shopmanagement.repository;

import com.shopmanagement.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByShopId(Long shopId);
    
    Optional<Wallet> findByReferralCode(String referralCode);
    
    Boolean existsByReferralCode(String referralCode);
}
