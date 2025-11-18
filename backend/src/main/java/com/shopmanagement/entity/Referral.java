package com.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "referrals", indexes = {
    @Index(name = "idx_referral_referrer_wallet_id", columnList = "referrer_wallet_id"),
    @Index(name = "idx_referral_referred_shop_id", columnList = "referred_shop_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_wallet_id", nullable = false)
    private Wallet referrerWallet;
    
    @Column(nullable = false)
    private Long referredShopId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bonusAmount;
    
    @Column(nullable = false)
    private Boolean bonusCredited = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
