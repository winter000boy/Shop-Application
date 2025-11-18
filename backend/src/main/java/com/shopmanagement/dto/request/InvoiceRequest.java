package com.shopmanagement.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    
    private Long repairOrderId;
    
    @Size(max = 5000, message = "Items description must not exceed 5000 characters")
    private String items;
    
    @DecimalMin(value = "0.0", message = "Subtotal must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Subtotal must have at most 10 integer digits and 2 decimal places")
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", message = "Tax amount must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal taxAmount;
}
