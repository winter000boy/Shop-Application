package com.shopmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatsResponse {
    
    private String referralCode;
    private Integer totalReferrals;
    private Integer creditedReferrals;
    private BigDecimal totalBonusEarned;
    private List<ReferralDetailResponse> referrals;
}
