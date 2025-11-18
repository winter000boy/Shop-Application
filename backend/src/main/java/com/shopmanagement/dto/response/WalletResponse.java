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
public class WalletResponse {
    
    private Long id;
    private Long shopId;
    private String shopName;
    private BigDecimal balance;
    private String referralCode;
    private LocalDateTime updatedAt;
}
