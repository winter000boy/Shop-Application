package com.shopmanagement.dto.response;

import com.shopmanagement.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private CustomerSummary customer;
    private String deviceModel;
    private String problemDescription;
    private BigDecimal estimatedPrice;
    private BigDecimal paidAmount;
    private String lockCode;
    private LocalDateTime repairDate;
    private String accessories;
    private String serialNumber;
    private StaffSummary assignedStaff;
    private OrderStatus status;
    private Boolean cashbackEnabled;
    private BigDecimal cashbackAmount;
    private Integer warrantyDays;
    private String expenses;
    private List<OrderImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String phoneNumber;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffSummary {
        private Long id;
        private String name;
        private String phoneNumber;
    }
}
