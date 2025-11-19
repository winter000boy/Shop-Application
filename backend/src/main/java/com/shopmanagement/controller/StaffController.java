package com.shopmanagement.controller;

import com.shopmanagement.dto.request.StaffRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.StaffPerformanceResponse;
import com.shopmanagement.dto.response.StaffResponse;
import com.shopmanagement.entity.Staff;
import com.shopmanagement.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "Endpoints for managing staff members, assignments, and performance metrics")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {
    
    private final StaffService staffService;
    
    /**
     * Get all staff members with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) Boolean activeOnly) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Staff> staffPage;
        
        if (activeOnly != null && activeOnly) {
            staffPage = staffService.getActiveStaff(pageable);
        } else {
            staffPage = staffService.getAllStaff(pageable);
        }
        
        List<StaffResponse> staffResponses = staffPage.getContent().stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", staffResponses);
        response.put("page", staffPage.getNumber());
        response.put("size", staffPage.getSize());
        response.put("totalElements", staffPage.getTotalElements());
        response.put("totalPages", staffPage.getTotalPages());
        response.put("last", staffPage.isLast());
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(response)
                .message("Staff members retrieved successfully")
                .build());
    }
    
    /**
     * Get staff member by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable Long id) {
        Staff staff = staffService.getStaffById(id);
        StaffResponse response = mapToStaffResponse(staff);
        
        return ResponseEntity.ok(ApiResponse.<StaffResponse>builder()
                .success(true)
                .data(response)
                .message("Staff member retrieved successfully")
                .build());
    }
    
    /**
     * Create a new staff member
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request) {
        
        // Validate password is provided for creation
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<StaffResponse>builder()
                            .success(false)
                            .message("Password is required for creating staff members")
                            .build());
        }
        
        Staff staff = mapToStaffEntity(request);
        Staff createdStaff = staffService.createStaff(staff, request.getPassword());
        StaffResponse response = mapToStaffResponse(createdStaff);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<StaffResponse>builder()
                        .success(true)
                        .data(response)
                        .message("Staff member created successfully")
                        .build());
    }
    
    /**
     * Update an existing staff member
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request) {
        
        Staff staffUpdate = mapToStaffEntity(request);
        Staff updatedStaff = staffService.updateStaff(id, staffUpdate);
        StaffResponse response = mapToStaffResponse(updatedStaff);
        
        return ResponseEntity.ok(ApiResponse.<StaffResponse>builder()
                .success(true)
                .data(response)
                .message("Staff member updated successfully")
                .build());
    }
    
    /**
     * Deactivate a staff member (soft delete)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(@PathVariable Long id) {
        staffService.deactivateStaff(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Staff member deactivated successfully")
                .build());
    }
    
    /**
     * Delete a staff member permanently
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Staff member deleted successfully")
                .build());
    }
    
    /**
     * Get staff performance metrics
     */
    @GetMapping("/{id}/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffPerformanceResponse>> getStaffPerformance(
            @PathVariable Long id) {
        
        Map<String, Object> performanceData = staffService.getStaffPerformance(id);
        StaffPerformanceResponse response = mapToPerformanceResponse(performanceData);
        
        return ResponseEntity.ok(ApiResponse.<StaffPerformanceResponse>builder()
                .success(true)
                .data(response)
                .message("Staff performance metrics retrieved successfully")
                .build());
    }
    
    /**
     * Map Staff entity to StaffResponse DTO
     */
    private StaffResponse mapToStaffResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .name(staff.getName())
                .phoneNumber(staff.getPhoneNumber())
                .email(staff.getEmail())
                .active(staff.getActive())
                .userId(staff.getUser() != null ? staff.getUser().getId() : null)
                .assignedOrdersCount(staff.getAssignedOrders() != null ? 
                        staff.getAssignedOrders().size() : 0)
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
    
    /**
     * Map StaffRequest DTO to Staff entity
     */
    private Staff mapToStaffEntity(StaffRequest request) {
        Staff staff = new Staff();
        staff.setName(request.getName());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setEmail(request.getEmail());
        return staff;
    }
    
    /**
     * Map performance data to StaffPerformanceResponse DTO
     */
    private StaffPerformanceResponse mapToPerformanceResponse(Map<String, Object> data) {
        return StaffPerformanceResponse.builder()
                .staffId(((Number) data.get("staffId")).longValue())
                .staffName((String) data.get("staffName"))
                .totalOrders(((Number) data.get("totalOrders")).longValue())
                .completedOrders(((Number) data.get("completedOrders")).longValue())
                .pendingOrders(((Number) data.get("pendingOrders")).longValue())
                .inProgressOrders(((Number) data.get("inProgressOrders")).longValue())
                .averageCompletionTimeHours((Double) data.get("averageCompletionTimeHours"))
                .completionRate((Double) data.get("completionRate"))
                .active((Boolean) data.get("active"))
                .build();
    }
}
