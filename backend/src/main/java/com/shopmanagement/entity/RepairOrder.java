package com.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_orders", indexes = {
    @Index(name = "idx_order_shop_id", columnList = "shop_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_customer_id", columnList = "customer_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long shopId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false)
    private String deviceModel;
    
    @Column(columnDefinition = "TEXT")
    private String problemDescription;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedPrice;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    private String lockCode;
    
    @Column(nullable = false)
    private LocalDateTime repairDate;
    
    @Column(columnDefinition = "TEXT")
    private String accessories;
    
    private String serialNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private Staff assignedStaff;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private Boolean cashbackEnabled = false;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cashbackAmount;
    
    private Integer warrantyDays;
    
    @Column(columnDefinition = "TEXT")
    private String expenses;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderImage> images = new ArrayList<>();
    
    @OneToOne(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Invoice invoice;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
