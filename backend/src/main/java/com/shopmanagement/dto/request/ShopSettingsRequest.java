package com.shopmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopSettingsRequest {
    
    // Settings fields can be extended based on requirements
    // Examples:
    private Integer defaultWarrantyDays;
    private Double defaultCashbackPercentage;
    private String currency;
    private Boolean emailNotificationsEnabled;
    private Boolean whatsappNotificationsEnabled;
}
