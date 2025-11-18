package com.shopmanagement.service;

import com.shopmanagement.entity.*;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.ValidationException;
import com.shopmanagement.repository.RepairOrderRepository;
import com.shopmanagement.repository.StaffRepository;
import com.shopmanagement.repository.UserRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {
    
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Get all staff members for the current shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<Staff> getAllStaff(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return staffRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Get active staff members for the current shop
     */
    @Transactional(readOnly = true)
    public Page<Staff> getActiveStaff(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return staffRepository.findByShopIdAndActive(shopId, true, pageable);
    }
    
    /**
     * Get staff member by ID (with shop-level isolation)
     */
    @Transactional(readOnly = true)
    public Staff getStaffById(Long staffId) {
        Long shopId = ShopContext.getCurrentShopId();
        return staffRepository.findByIdAndShopId(staffId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff member not found with id: " + staffId));
    }
    
    /**
     * Create a new staff member and associated user account
     */
    @Transactional
    public Staff createStaff(Staff staff, String password) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Validate email uniqueness if provided
        if (staff.getEmail() != null && !staff.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(staff.getEmail())) {
                throw new ValidationException("Email " + staff.getEmail() + " is already registered");
            }
        } else {
            throw new ValidationException("Email is required for staff members");
        }
        
        // Validate password
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required for staff members");
        }
        
        // Set shopId for multi-tenant isolation
        staff.setShopId(shopId);
        staff.setActive(true);
        
        // Save staff first
        Staff savedStaff = staffRepository.save(staff);
        
        // Create user account for staff member
        User user = new User();
        user.setEmail(staff.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(staff.getName());
        user.setRole(UserRole.STAFF);
        
        // Get shop reference
        User adminUser = userRepository.findById(ShopContext.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        user.setShop(adminUser.getShop());
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Link user to staff
        savedStaff.setUser(savedUser);
        
        log.info("Created staff member: {} with user account: {}", savedStaff.getName(), savedUser.getEmail());
        
        return staffRepository.save(savedStaff);
    }
    
    /**
     * Update an existing staff member
     */
    @Transactional
    public Staff updateStaff(Long staffId, Staff staffUpdate) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Staff existingStaff = staffRepository.findByIdAndShopId(staffId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff member not found with id: " + staffId));
        
        // Validate email uniqueness if changed
        if (staffUpdate.getEmail() != null && !staffUpdate.getEmail().trim().isEmpty()) {
            if (!staffUpdate.getEmail().equals(existingStaff.getEmail())) {
                if (userRepository.existsByEmail(staffUpdate.getEmail())) {
                    throw new ValidationException("Email " + staffUpdate.getEmail() + " is already registered");
                }
                
                // Update associated user email
                if (existingStaff.getUser() != null) {
                    User user = existingStaff.getUser();
                    user.setEmail(staffUpdate.getEmail());
                    userRepository.save(user);
                }
            }
        }
        
        // Update fields
        existingStaff.setName(staffUpdate.getName());
        existingStaff.setPhoneNumber(staffUpdate.getPhoneNumber());
        existingStaff.setEmail(staffUpdate.getEmail());
        
        // Update associated user full name
        if (existingStaff.getUser() != null) {
            User user = existingStaff.getUser();
            user.setFullName(staffUpdate.getName());
            userRepository.save(user);
        }
        
        log.info("Updated staff member: {}", existingStaff.getName());
        
        return staffRepository.save(existingStaff);
    }
    
    /**
     * Deactivate a staff member (soft delete)
     */
    @Transactional
    public void deactivateStaff(Long staffId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Staff staff = staffRepository.findByIdAndShopId(staffId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff member not found with id: " + staffId));
        
        // Deactivate staff
        staff.setActive(false);
        
        // Deactivate associated user account
        if (staff.getUser() != null) {
            User user = staff.getUser();
            user.setActive(false);
            userRepository.save(user);
        }
        
        staffRepository.save(staff);
        
        log.info("Deactivated staff member: {}", staff.getName());
    }
    
    /**
     * Permanently delete a staff member
     */
    @Transactional
    public void deleteStaff(Long staffId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Staff staff = staffRepository.findByIdAndShopId(staffId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff member not found with id: " + staffId));
        
        // Check if staff has assigned orders
        Page<RepairOrder> assignedOrders = repairOrderRepository.findByShopIdAndAssignedStaffId(
                shopId, staffId, Pageable.unpaged());
        
        if (!assignedOrders.isEmpty()) {
            throw new ValidationException(
                    "Cannot delete staff member with assigned repair orders. " +
                    "Staff has " + assignedOrders.getTotalElements() + " assigned order(s). " +
                    "Please deactivate instead.");
        }
        
        // Delete associated user account
        if (staff.getUser() != null) {
            userRepository.delete(staff.getUser());
        }
        
        staffRepository.delete(staff);
        
        log.info("Deleted staff member: {}", staff.getName());
    }
    
    /**
     * Get performance metrics for a staff member
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStaffPerformance(Long staffId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Staff staff = staffRepository.findByIdAndShopId(staffId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff member not found with id: " + staffId));
        
        // Get all assigned orders
        Page<RepairOrder> allOrders = repairOrderRepository.findByShopIdAndAssignedStaffId(
                shopId, staffId, Pageable.unpaged());
        
        List<RepairOrder> orders = allOrders.getContent();
        
        // Calculate metrics
        long totalOrders = orders.size();
        long completedOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED || 
                               order.getStatus() == OrderStatus.DELIVERED)
                .count();
        long pendingOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();
        long inProgressOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.IN_PROGRESS)
                .count();
        
        // Calculate average completion time for completed orders
        List<RepairOrder> completedOrdersList = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED || 
                               order.getStatus() == OrderStatus.DELIVERED)
                .toList();
        
        double averageCompletionTimeHours = 0.0;
        if (!completedOrdersList.isEmpty()) {
            long totalCompletionTimeMinutes = completedOrdersList.stream()
                    .mapToLong(order -> {
                        Duration duration = Duration.between(order.getCreatedAt(), order.getUpdatedAt());
                        return duration.toMinutes();
                    })
                    .sum();
            
            averageCompletionTimeHours = (totalCompletionTimeMinutes / (double) completedOrdersList.size()) / 60.0;
        }
        
        // Calculate completion rate
        double completionRate = totalOrders > 0 ? 
                (completedOrders * 100.0) / totalOrders : 0.0;
        
        // Build performance metrics map
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("staffId", staffId);
        metrics.put("staffName", staff.getName());
        metrics.put("totalOrders", totalOrders);
        metrics.put("completedOrders", completedOrders);
        metrics.put("pendingOrders", pendingOrders);
        metrics.put("inProgressOrders", inProgressOrders);
        metrics.put("averageCompletionTimeHours", Math.round(averageCompletionTimeHours * 100.0) / 100.0);
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        metrics.put("active", staff.getActive());
        
        return metrics;
    }
}
