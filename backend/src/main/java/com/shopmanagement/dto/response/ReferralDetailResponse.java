package com.shopmanagement.dto.response;

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
public class ReferralDetailResponse {
    
    private Long id;
    private Long referredShopId;
    private String referredShopName;
    private BigDecimal bonusAmount;
    private Boolean bonusCredited;
    private LocalDateTime createdAt;
}
