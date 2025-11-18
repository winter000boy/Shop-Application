package com.shopmanagement.dto.request;

import com.shopmanagement.entity.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotBlank(message = "Device model is required")
    @Size(min = 2, max = 100, message = "Device model must be between 2 and 100 characters")
    private String deviceModel;
    
    @Size(max = 2000, message = "Problem description must not exceed 2000 characters")
    private String problemDescription;
    
    @NotNull(message = "Estimated price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Estimated price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal estimatedPrice;
    
    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.0", message = "Paid amount must be 0 or greater")
    @Digits(integer = 8, fraction = 2, message = "Paid amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal paidAmount;
    
    @Size(max = 50, message = "Lock code must not exceed 50 characters")
    private String lockCode;
    
    @NotNull(message = "Repair date is required")
    private LocalDateTime repairDate;
    
    @Size(max = 1000, message = "Accessories must not exceed 1000 characters")
    private String accessories;
    
    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    private String serialNumber;
    
    private Long assignedStaffId;
    
    private OrderStatus status;
    
    private Boolean cashbackEnabled;
    
    @Min(value = 0, message = "Warranty days must be 0 or greater")
    @Max(value = 3650, message = "Warranty days must not exceed 3650 (10 years)")
    private Integer warrantyDays;
    
    @Size(max = 2000, message = "Expenses must not exceed 2000 characters")
    private String expenses;
}
