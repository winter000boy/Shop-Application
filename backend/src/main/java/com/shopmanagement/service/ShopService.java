package com.shopmanagement.service;

import com.shopmanagement.entity.Shop;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ShopService {
    
    private final ShopRepository shopRepository;
    private final StorageService storageService;
    
    /**
     * Get the profile of the currently authenticated shop
     */
    @Transactional(readOnly = true)
    public Shop getShopProfile() {
        Long shopId = ShopContext.getCurrentShopId();
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
    }
    
    /**
     * Update shop profile information
     */
    @Transactional
    public Shop updateShopProfile(Shop updatedShop) {
        Long shopId = ShopContext.getCurrentShopId();
        Shop existingShop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        // Update allowed fields
        if (updatedShop.getShopName() != null) {
            existingShop.setShopName(updatedShop.getShopName());
        }
        if (updatedShop.getShopType() != null) {
            existingShop.setShopType(updatedShop.getShopType());
        }
        if (updatedShop.getGstNumber() != null) {
            existingShop.setGstNumber(updatedShop.getGstNumber());
        }
        if (updatedShop.getOwnerName() != null) {
            existingShop.setOwnerName(updatedShop.getOwnerName());
        }
        if (updatedShop.getPhoneNumber() != null) {
            existingShop.setPhoneNumber(updatedShop.getPhoneNumber());
        }
        if (updatedShop.getCountryCode() != null) {
            existingShop.setCountryCode(updatedShop.getCountryCode());
        }
        if (updatedShop.getAddress() != null) {
            existingShop.setAddress(updatedShop.getAddress());
        }
        
        return shopRepository.save(existingShop);
    }
    
    /**
     * Upload and update shop logo
     */
    @Transactional
    public Shop updateShopLogo(MultipartFile logoFile) {
        Long shopId = ShopContext.getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        // Delete old logo if exists
        if (shop.getLogoUrl() != null && !shop.getLogoUrl().isEmpty()) {
            storageService.deleteFile(shop.getLogoUrl());
        }
        
        // Upload new logo
        String logoUrl = storageService.uploadFile(logoFile, "shop-logos");
        shop.setLogoUrl(logoUrl);
        
        return shopRepository.save(shop);
    }
    
    /**
     * Update shop settings (can be extended with specific settings)
     */
    @Transactional
    public Shop updateShopSettings(Shop settingsUpdate) {
        Long shopId = ShopContext.getCurrentShopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        // Update settings fields as needed
        // This can be extended with specific settings fields
        
        return shopRepository.save(shop);
    }
}
