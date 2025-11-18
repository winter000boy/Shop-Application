package com.shopmanagement.service;

import com.shopmanagement.entity.*;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.ValidationException;
import com.shopmanagement.repository.CustomerRepository;
import com.shopmanagement.repository.OrderImageRepository;
import com.shopmanagement.repository.RepairOrderRepository;
import com.shopmanagement.repository.StaffRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final RepairOrderRepository repairOrderRepository;
    private final OrderImageRepository orderImageRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final StorageService storageService;
    
    private static final BigDecimal CASHBACK_PERCENTAGE = new BigDecimal("0.05"); // 5% cashback
    
    /**
     * Get all repair orders for the current shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> getAllOrders(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Get repair orders filtered by status
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.findByShopIdAndStatus(shopId, status, pageable);
    }
    
    /**
     * Get repair orders by date range
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.findByShopIdAndDateRange(shopId, startDate, endDate, pageable);
    }
    
    /**
     * Get repair orders by status and date range
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> getOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, 
                                                           LocalDateTime endDate, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.findByShopIdAndStatusAndDateRange(shopId, status, startDate, endDate, pageable);
    }
    
    /**
     * Search repair orders by device model or serial number
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> searchOrders(String searchTerm, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return repairOrderRepository.findByShopId(shopId, pageable);
        }
        
        return repairOrderRepository.searchOrders(shopId, searchTerm.trim(), pageable);
    }
    
    /**
     * Get repair orders for a specific customer
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> getOrdersByCustomer(Long customerId, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Verify customer belongs to shop
        customerRepository.findByIdAndShopId(customerId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        
        return repairOrderRepository.findByShopIdAndCustomerId(shopId, customerId, pageable);
    }
    
    /**
     * Get repair orders assigned to a specific staff member
     */
    @Transactional(readOnly = true)
    public Page<RepairOrder> getOrdersByStaff(Long staffId, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.findByShopIdAndAssignedStaffId(shopId, staffId, pageable);
    }
    
    /**
     * Get repair order by ID (with shop-level isolation)
     */
    @Transactional(readOnly = true)
    public RepairOrder getOrderById(Long orderId) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
    }
    
    /**
     * Create a new repair order
     */
    @Transactional
    public RepairOrder createOrder(RepairOrder order) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Validate customer exists and belongs to shop
        Customer customer = customerRepository.findByIdAndShopId(order.getCustomer().getId(), shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + order.getCustomer().getId()));
        
        // Validate staff if assigned
        if (order.getAssignedStaff() != null && order.getAssignedStaff().getId() != null) {
            Staff staff = staffRepository.findByIdAndShopId(order.getAssignedStaff().getId(), shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Staff not found with id: " + order.getAssignedStaff().getId()));
            order.setAssignedStaff(staff);
        }
        
        // Set shopId for multi-tenant isolation
        order.setShopId(shopId);
        order.setCustomer(customer);
        
        // Calculate cashback if enabled
        if (order.getCashbackEnabled() != null && order.getCashbackEnabled()) {
            order.setCashbackAmount(calculateCashback(order.getPaidAmount()));
        }
        
        // Set default status if not provided
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        
        log.info("Creating repair order for customer: {} in shop: {}", customer.getId(), shopId);
        return repairOrderRepository.save(order);
    }
    
    /**
     * Update an existing repair order
     */
    @Transactional
    public RepairOrder updateOrder(Long orderId, RepairOrder orderUpdate) {
        Long shopId = ShopContext.getCurrentShopId();
        
        RepairOrder existingOrder = repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
        
        // Validate customer if changed
        if (orderUpdate.getCustomer() != null && orderUpdate.getCustomer().getId() != null) {
            Customer customer = customerRepository.findByIdAndShopId(orderUpdate.getCustomer().getId(), shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found with id: " + orderUpdate.getCustomer().getId()));
            existingOrder.setCustomer(customer);
        }
        
        // Validate staff if changed
        if (orderUpdate.getAssignedStaff() != null && orderUpdate.getAssignedStaff().getId() != null) {
            Staff staff = staffRepository.findByIdAndShopId(orderUpdate.getAssignedStaff().getId(), shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Staff not found with id: " + orderUpdate.getAssignedStaff().getId()));
            existingOrder.setAssignedStaff(staff);
        } else if (orderUpdate.getAssignedStaff() == null) {
            existingOrder.setAssignedStaff(null);
        }
        
        // Update fields
        existingOrder.setDeviceModel(orderUpdate.getDeviceModel());
        existingOrder.setProblemDescription(orderUpdate.getProblemDescription());
        existingOrder.setEstimatedPrice(orderUpdate.getEstimatedPrice());
        existingOrder.setPaidAmount(orderUpdate.getPaidAmount());
        existingOrder.setLockCode(orderUpdate.getLockCode());
        existingOrder.setRepairDate(orderUpdate.getRepairDate());
        existingOrder.setAccessories(orderUpdate.getAccessories());
        existingOrder.setSerialNumber(orderUpdate.getSerialNumber());
        existingOrder.setWarrantyDays(orderUpdate.getWarrantyDays());
        existingOrder.setExpenses(orderUpdate.getExpenses());
        
        // Update cashback if enabled status changed
        if (orderUpdate.getCashbackEnabled() != null) {
            existingOrder.setCashbackEnabled(orderUpdate.getCashbackEnabled());
            if (orderUpdate.getCashbackEnabled()) {
                existingOrder.setCashbackAmount(calculateCashback(existingOrder.getPaidAmount()));
            } else {
                existingOrder.setCashbackAmount(null);
            }
        }
        
        log.info("Updating repair order: {} in shop: {}", orderId, shopId);
        return repairOrderRepository.save(existingOrder);
    }
    
    /**
     * Update repair order status
     */
    @Transactional
    public RepairOrder updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Long shopId = ShopContext.getCurrentShopId();
        
        RepairOrder order = repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        log.info("Updating order {} status from {} to {} in shop: {}", orderId, oldStatus, newStatus, shopId);
        return repairOrderRepository.save(order);
    }
    
    /**
     * Delete a repair order
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        RepairOrder order = repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
        
        // Delete associated images from storage
        for (OrderImage image : order.getImages()) {
            try {
                storageService.deleteFile(image.getImageUrl());
            } catch (Exception e) {
                log.error("Failed to delete image: {}", image.getImageUrl(), e);
            }
        }
        
        log.info("Deleting repair order: {} in shop: {}", orderId, shopId);
        repairOrderRepository.delete(order);
    }
    
    /**
     * Upload images for a repair order
     */
    @Transactional
    public List<OrderImage> uploadOrderImages(Long orderId, List<MultipartFile> files) {
        Long shopId = ShopContext.getCurrentShopId();
        
        RepairOrder order = repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
        
        List<OrderImage> uploadedImages = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // Upload file to storage
                String imageUrl = storageService.uploadFile(file, "orders/" + orderId);
                
                // Create OrderImage entity
                OrderImage orderImage = new OrderImage();
                orderImage.setOrder(order);
                orderImage.setImageUrl(imageUrl);
                
                OrderImage savedImage = orderImageRepository.save(orderImage);
                uploadedImages.add(savedImage);
                
                log.info("Uploaded image for order {}: {}", orderId, imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload image for order: {}", orderId, e);
                throw new ValidationException("Failed to upload image: " + e.getMessage());
            }
        }
        
        return uploadedImages;
    }
    
    /**
     * Delete an order image
     */
    @Transactional
    public void deleteOrderImage(Long orderId, Long imageId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Verify order belongs to shop
        RepairOrder order = repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
        
        // Find and delete image
        OrderImage image = orderImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        // Verify image belongs to the order
        if (!image.getOrder().getId().equals(orderId)) {
            throw new ValidationException("Image does not belong to this order");
        }
        
        // Delete from storage
        try {
            storageService.deleteFile(image.getImageUrl());
        } catch (Exception e) {
            log.error("Failed to delete image from storage: {}", image.getImageUrl(), e);
        }
        
        orderImageRepository.delete(image);
        log.info("Deleted image {} for order {}", imageId, orderId);
    }
    
    /**
     * Get order count by status
     */
    @Transactional(readOnly = true)
    public Long getOrderCountByStatus(OrderStatus status) {
        Long shopId = ShopContext.getCurrentShopId();
        return repairOrderRepository.countByShopIdAndStatus(shopId, status);
    }
    
    /**
     * Calculate cashback amount (5% of paid amount)
     */
    private BigDecimal calculateCashback(BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return paidAmount.multiply(CASHBACK_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    }
}
