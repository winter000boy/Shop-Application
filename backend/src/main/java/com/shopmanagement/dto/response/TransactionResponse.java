package com.shopmanagement.dto.response;

import com.shopmanagement.entity.TransactionType;
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
public class TransactionResponse {
    
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
