package com.shopmanagement.dto.response;

import com.shopmanagement.entity.ShopType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProfileResponse {
    
    private Long id;
    private String shopName;
    private ShopType shopType;
    private String gstNumber;
    private String ownerName;
    private String username;
    private String phoneNumber;
    private String countryCode;
    private String address;
    private String email;
    private String logoUrl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
