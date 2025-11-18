package com.shopmanagement.service;

import com.shopmanagement.entity.*;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.ValidationException;
import com.shopmanagement.repository.ReferralRepository;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.repository.TransactionRepository;
import com.shopmanagement.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ReferralRepository referralRepository;
    private final ShopRepository shopRepository;
    
    @Value("${app.referral.bonus:100.00}")
    private BigDecimal referralBonusAmount;
    
    /**
     * Get wallet by shop ID
     */
    public Wallet getWalletByShopId(Long shopId) {
        return walletRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for shop: " + shopId));
    }
    
    /**
     * Get wallet balance for a shop
     */
    public BigDecimal getBalance(Long shopId) {
        Wallet wallet = getWalletByShopId(shopId);
        return wallet.getBalance();
    }
    
    /**
     * Get transaction history for a shop
     */
    public Page<Transaction> getTransactionHistory(Long shopId, Pageable pageable) {
        return transactionRepository.findByWalletShopIdOrderByCreatedAtDesc(shopId, pageable);
    }
    
    /**
     * Credit amount to wallet
     */
    @Transactional
    public Transaction creditWallet(Long shopId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Credit amount must be positive");
        }
        
        Wallet wallet = getWalletByShopId(shopId);
        
        // Update balance
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        
        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.CREDIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfter(newBalance);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Credited {} to wallet for shop {}. New balance: {}", amount, shopId, newBalance);
        
        return savedTransaction;
    }
    
    /**
     * Debit amount from wallet
     */
    @Transactional
    public Transaction debitWallet(Long shopId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Debit amount must be positive");
        }
        
        Wallet wallet = getWalletByShopId(shopId);
        
        // Check sufficient balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Insufficient wallet balance");
        }
        
        // Update balance
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        
        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setBalanceAfter(newBalance);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Debited {} from wallet for shop {}. New balance: {}", amount, shopId, newBalance);
        
        return savedTransaction;
    }
    
    /**
     * Get referral code for a shop
     */
    public String getReferralCode(Long shopId) {
        Wallet wallet = getWalletByShopId(shopId);
        return wallet.getReferralCode();
    }
    
    /**
     * Get referral statistics for a shop
     */
    public List<Referral> getReferralStats(Long shopId) {
        Wallet wallet = getWalletByShopId(shopId);
        return referralRepository.findByReferrerWalletId(wallet.getId());
    }
    
    /**
     * Apply referral code during shop registration
     * This should be called from AuthService during registration
     */
    @Transactional
    public void applyReferralCode(String referralCode, Long newShopId) {
        if (referralCode == null || referralCode.trim().isEmpty()) {
            return; // No referral code provided
        }
        
        // Check if this shop has already used a referral code
        if (referralRepository.existsByReferredShopId(newShopId)) {
            throw new ValidationException("This shop has already used a referral code");
        }
        
        // Find the referrer's wallet
        Wallet referrerWallet = walletRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new ValidationException("Invalid referral code"));
        
        // Prevent self-referral
        if (referrerWallet.getShop().getId().equals(newShopId)) {
            throw new ValidationException("Cannot use your own referral code");
        }
        
        // Create referral record
        Referral referral = new Referral();
        referral.setReferrerWallet(referrerWallet);
        referral.setReferredShopId(newShopId);
        referral.setBonusAmount(referralBonusAmount);
        referral.setBonusCredited(false);
        
        referralRepository.save(referral);
        
        log.info("Referral code {} applied for new shop {}", referralCode, newShopId);
    }
    
    /**
     * Credit referral bonus to referrer
     * This should be called after the referred shop completes certain actions
     */
    @Transactional
    public void creditReferralBonus(Long referredShopId) {
        Referral referral = referralRepository.findByReferredShopId(referredShopId)
                .orElse(null);
        
        if (referral == null) {
            log.debug("No referral found for shop {}", referredShopId);
            return;
        }
        
        if (referral.getBonusCredited()) {
            log.debug("Referral bonus already credited for shop {}", referredShopId);
            return;
        }
        
        // Credit bonus to referrer's wallet
        Wallet referrerWallet = referral.getReferrerWallet();
        Long referrerShopId = referrerWallet.getShop().getId();
        
        Shop referredShop = shopRepository.findById(referredShopId)
                .orElseThrow(() -> new ResourceNotFoundException("Referred shop not found"));
        
        String description = String.format("Referral bonus for referring %s", referredShop.getShopName());
        
        creditWallet(referrerShopId, referral.getBonusAmount(), description);
        
        // Mark bonus as credited
        referral.setBonusCredited(true);
        referralRepository.save(referral);
        
        log.info("Credited referral bonus of {} to shop {} for referring shop {}", 
                referral.getBonusAmount(), referrerShopId, referredShopId);
    }
}
