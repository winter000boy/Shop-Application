package com.shopmanagement.controller;

import com.shopmanagement.dto.request.ShopSettingsRequest;
import com.shopmanagement.dto.request.ShopUpdateRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.ShopProfileResponse;
import com.shopmanagement.entity.Shop;
import com.shopmanagement.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {
    
    private final ShopService shopService;
    
    /**
     * Get current shop profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getShopProfile() {
        Shop shop = shopService.getShopProfile();
        ShopProfileResponse response = mapToProfileResponse(shop);
        
        return ResponseEntity.ok(ApiResponse.<ShopProfileResponse>builder()
                .success(true)
                .data(response)
                .message("Shop profile retrieved successfully")
                .build());
    }
    
    /**
     * Update shop profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> updateShopProfile(
            @Valid @RequestBody ShopUpdateRequest request) {
        
        Shop shopUpdate = mapToShopEntity(request);
        Shop updatedShop = shopService.updateShopProfile(shopUpdate);
        ShopProfileResponse response = mapToProfileResponse(updatedShop);
        
        return ResponseEntity.ok(ApiResponse.<ShopProfileResponse>builder()
                .success(true)
                .data(response)
                .message("Shop profile updated successfully")
                .build());
    }
    
    /**
     * Upload shop logo
     */
    @PutMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> updateShopLogo(
            @RequestParam("logo") MultipartFile logoFile) {
        
        Shop updatedShop = shopService.updateShopLogo(logoFile);
        ShopProfileResponse response = mapToProfileResponse(updatedShop);
        
        return ResponseEntity.ok(ApiResponse.<ShopProfileResponse>builder()
                .success(true)
                .data(response)
                .message("Shop logo updated successfully")
                .build());
    }
    
    /**
     * Update shop settings
     */
    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> updateShopSettings(
            @Valid @RequestBody ShopSettingsRequest request) {
        
        // For now, settings are stored as part of shop entity
        // This can be extended with a separate Settings entity if needed
        Shop settingsUpdate = new Shop();
        // Map settings to shop entity as needed
        
        Shop updatedShop = shopService.updateShopSettings(settingsUpdate);
        ShopProfileResponse response = mapToProfileResponse(updatedShop);
        
        return ResponseEntity.ok(ApiResponse.<ShopProfileResponse>builder()
                .success(true)
                .data(response)
                .message("Shop settings updated successfully")
                .build());
    }
    
    /**
     * Map Shop entity to ShopProfileResponse DTO
     */
    private ShopProfileResponse mapToProfileResponse(Shop shop) {
        return ShopProfileResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .shopType(shop.getShopType())
                .gstNumber(shop.getGstNumber())
                .ownerName(shop.getOwnerName())
                .username(shop.getUsername())
                .phoneNumber(shop.getPhoneNumber())
                .countryCode(shop.getCountryCode())
                .address(shop.getAddress())
                .email(shop.getEmail())
                .logoUrl(shop.getLogoUrl())
                .active(shop.getActive())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .build();
    }
    
    /**
     * Map ShopUpdateRequest DTO to Shop entity
     */
    private Shop mapToShopEntity(ShopUpdateRequest request) {
        Shop shop = new Shop();
        shop.setShopName(request.getShopName());
        shop.setShopType(request.getShopType());
        shop.setGstNumber(request.getGstNumber());
        shop.setOwnerName(request.getOwnerName());
        shop.setPhoneNumber(request.getPhoneNumber());
        shop.setCountryCode(request.getCountryCode());
        shop.setAddress(request.getAddress());
        return shop;
    }
}
