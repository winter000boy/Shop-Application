package com.shopmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformanceResponse {
    
    private Long staffId;
    private String staffName;
    private Long totalOrders;
    private Long completedOrders;
    private Long pendingOrders;
    private Long inProgressOrders;
    private Double averageCompletionTimeHours;
    private Double completionRate;
    private Boolean active;
}
