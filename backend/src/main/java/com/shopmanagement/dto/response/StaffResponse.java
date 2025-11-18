package com.shopmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private Boolean active;
    private Long userId;
    private Integer assignedOrdersCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
