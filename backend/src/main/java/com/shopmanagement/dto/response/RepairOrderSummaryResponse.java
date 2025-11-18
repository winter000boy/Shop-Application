package com.shopmanagement.dto.response;

import com.shopmanagement.entity.OrderStatus;
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
public class RepairOrderSummaryResponse {
    
    private Long id;
    private String deviceModel;
    private String problemDescription;
    private BigDecimal estimatedPrice;
    private BigDecimal paidAmount;
    private OrderStatus status;
    private LocalDateTime repairDate;
    private String assignedStaffName;
    private Integer warrantyDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
