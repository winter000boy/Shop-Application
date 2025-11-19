package com.shopmanagement.controller;

import com.shopmanagement.dto.request.CustomerRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.CustomerResponse;
import com.shopmanagement.dto.response.RepairOrderSummaryResponse;
import com.shopmanagement.entity.Customer;
import com.shopmanagement.entity.RepairOrder;
import com.shopmanagement.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Endpoints for managing customer information and repair history")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {
    
    private final CustomerService customerService;
    
    /**
     * Get all customers with pagination and optional search
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String searchBy) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Customer> customerPage;
        
        // Handle different search scenarios
        if (search != null && !search.trim().isEmpty()) {
            if ("name".equalsIgnoreCase(searchBy)) {
                customerPage = customerService.searchByName(search, pageable);
            } else if ("phone".equalsIgnoreCase(searchBy)) {
                customerPage = customerService.searchByPhone(search, pageable);
            } else if ("email".equalsIgnoreCase(searchBy)) {
                customerPage = customerService.searchByEmail(search, pageable);
            } else {
                // Default: search across all fields
                customerPage = customerService.searchCustomers(search, pageable);
            }
        } else {
            customerPage = customerService.getAllCustomers(pageable);
        }
        
        List<CustomerResponse> customerResponses = customerPage.getContent().stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", customerResponses);
        response.put("page", customerPage.getNumber());
        response.put("size", customerPage.getSize());
        response.put("totalElements", customerPage.getTotalElements());
        response.put("totalPages", customerPage.getTotalPages());
        response.put("last", customerPage.isLast());
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(response)
                .message("Customers retrieved successfully")
                .build());
    }
    
    /**
     * Get customer by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        CustomerResponse response = mapToCustomerResponse(customer);
        
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .data(response)
                .message("Customer retrieved successfully")
                .build());
    }
    
    /**
     * Create a new customer
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        
        Customer customer = mapToCustomerEntity(request);
        Customer createdCustomer = customerService.createCustomer(customer);
        CustomerResponse response = mapToCustomerResponse(createdCustomer);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CustomerResponse>builder()
                        .success(true)
                        .data(response)
                        .message("Customer created successfully")
                        .build());
    }
    
    /**
     * Update an existing customer
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        
        Customer customerUpdate = mapToCustomerEntity(request);
        Customer updatedCustomer = customerService.updateCustomer(id, customerUpdate);
        CustomerResponse response = mapToCustomerResponse(updatedCustomer);
        
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .data(response)
                .message("Customer updated successfully")
                .build());
    }
    
    /**
     * Delete a customer
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Customer deleted successfully")
                .build());
    }
    
    /**
     * Get customer repair order history
     */
    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<RepairOrderSummaryResponse>>> getCustomerOrders(
            @PathVariable Long id) {
        
        Customer customer = customerService.getCustomerById(id);
        
        List<RepairOrderSummaryResponse> orderResponses = customer.getRepairOrders().stream()
                .map(this::mapToRepairOrderSummary)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.<List<RepairOrderSummaryResponse>>builder()
                .success(true)
                .data(orderResponses)
                .message("Customer repair orders retrieved successfully")
                .build());
    }
    
    /**
     * Map Customer entity to CustomerResponse DTO
     */
    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .repairOrderCount(customer.getRepairOrders() != null ? 
                        customer.getRepairOrders().size() : 0)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
    
    /**
     * Map CustomerRequest DTO to Customer entity
     */
    private Customer mapToCustomerEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        return customer;
    }
    
    /**
     * Map RepairOrder entity to RepairOrderSummaryResponse DTO
     */
    private RepairOrderSummaryResponse mapToRepairOrderSummary(RepairOrder order) {
        return RepairOrderSummaryResponse.builder()
                .id(order.getId())
                .deviceModel(order.getDeviceModel())
                .problemDescription(order.getProblemDescription())
                .estimatedPrice(order.getEstimatedPrice())
                .paidAmount(order.getPaidAmount())
                .status(order.getStatus())
                .repairDate(order.getRepairDate())
                .assignedStaffName(order.getAssignedStaff() != null ? 
                        order.getAssignedStaff().getName() : null)
                .warrantyDays(order.getWarrantyDays())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
