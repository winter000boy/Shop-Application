package com.shopmanagement.dto.request;

import jakarta.validation.constraints.*;
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
    @Min(value = 0, message = "Default warranty days must be 0 or greater")
    @Max(value = 3650, message = "Default warranty days must not exceed 3650 (10 years)")
    private Integer defaultWarrantyDays;
    
    @DecimalMin(value = "0.0", message = "Default cashback percentage must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Default cashback percentage must not exceed 100")
    private Double defaultCashbackPercentage;
    
    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;
    
    private Boolean emailNotificationsEnabled;
    private Boolean whatsappNotificationsEnabled;
}
