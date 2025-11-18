package com.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_shop_id", columnList = "shop_id"),
    @Index(name = "idx_wallet_referral_code", columnList = "referral_code")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();
    
    @Column(unique = true, nullable = false)
    private String referralCode;
    
    @OneToMany(mappedBy = "referrerWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Referral> referrals = new ArrayList<>();
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
