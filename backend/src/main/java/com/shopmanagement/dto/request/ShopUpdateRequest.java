package com.shopmanagement.dto.request;

import com.shopmanagement.entity.ShopType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopUpdateRequest {
    
    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    private String shopName;
    
    private ShopType shopType;
    
    @Size(max = 20, message = "GST number must not exceed 20 characters")
    private String gstNumber;
    
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    private String ownerName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;
    
    @Size(max = 5, message = "Country code must not exceed 5 characters")
    private String countryCode;
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String address;
}
