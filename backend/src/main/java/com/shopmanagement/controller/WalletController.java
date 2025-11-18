package com.shopmanagement.controller;

import com.shopmanagement.dto.request.ApplyReferralRequest;
import com.shopmanagement.dto.response.*;
import com.shopmanagement.entity.Referral;
import com.shopmanagement.entity.Transaction;
import com.shopmanagement.entity.Wallet;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.security.ShopContext;
import com.shopmanagement.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    private final ShopRepository shopRepository;
    
    /**
     * Get wallet information
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet() {
        Long shopId = ShopContext.getCurrentShopId();
        Wallet wallet = walletService.getWalletByShopId(shopId);
        
        WalletResponse response = WalletResponse.builder()
                .id(wallet.getId())
                .shopId(wallet.getShop().getId())
                .shopName(wallet.getShop().getShopName())
                .balance(wallet.getBalance())
                .referralCode(wallet.getReferralCode())
                .updatedAt(wallet.getUpdatedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet retrieved successfully"));
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long shopId = ShopContext.getCurrentShopId();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Transaction> transactions = walletService.getTransactionHistory(shopId, pageable);
        
        Page<TransactionResponse> response = transactions.map(transaction ->
                TransactionResponse.builder()
                        .id(transaction.getId())
                        .type(transaction.getType())
                        .amount(transaction.getAmount())
                        .description(transaction.getDescription())
                        .balanceAfter(transaction.getBalanceAfter())
                        .createdAt(transaction.getCreatedAt())
                        .build()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Transactions retrieved successfully"));
    }
    
    /**
     * Get referral code and statistics
     */
    @GetMapping("/referral")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ReferralStatsResponse>> getReferralStats() {
        Long shopId = ShopContext.getCurrentShopId();
        
        String referralCode = walletService.getReferralCode(shopId);
        List<Referral> referrals = walletService.getReferralStats(shopId);
        
        List<ReferralDetailResponse> referralDetails = referrals.stream()
                .map(referral -> {
                    String referredShopName = shopRepository.findById(referral.getReferredShopId())
                            .map(shop -> shop.getShopName())
                            .orElse("Unknown Shop");
                    
                    return ReferralDetailResponse.builder()
                            .id(referral.getId())
                            .referredShopId(referral.getReferredShopId())
                            .referredShopName(referredShopName)
                            .bonusAmount(referral.getBonusAmount())
                            .bonusCredited(referral.getBonusCredited())
                            .createdAt(referral.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        
        long creditedCount = referrals.stream()
                .filter(Referral::getBonusCredited)
                .count();
        
        BigDecimal totalBonus = referrals.stream()
                .filter(Referral::getBonusCredited)
                .map(Referral::getBonusAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        ReferralStatsResponse response = ReferralStatsResponse.builder()
                .referralCode(referralCode)
                .totalReferrals(referrals.size())
                .creditedReferrals((int) creditedCount)
                .totalBonusEarned(totalBonus)
                .referrals(referralDetails)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Referral stats retrieved successfully"));
    }
    
    /**
     * Apply referral code (for existing shops that didn't use one during registration)
     * Note: This is typically done during registration, but this endpoint allows
     * applying a referral code post-registration if needed
     */
    @PostMapping("/referral/apply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> applyReferralCode(
            @Valid @RequestBody ApplyReferralRequest request) {
        
        Long shopId = ShopContext.getCurrentShopId();
        
        walletService.applyReferralCode(request.getReferralCode(), shopId);
        walletService.creditReferralBonus(shopId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Referral code applied successfully",
                "Referral code applied successfully"
        ));
    }
}
