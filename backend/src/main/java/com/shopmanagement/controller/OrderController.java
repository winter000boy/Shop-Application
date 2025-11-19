package com.shopmanagement.controller;

import com.shopmanagement.dto.request.OrderRequest;
import com.shopmanagement.dto.request.OrderStatusUpdateRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.OrderImageResponse;
import com.shopmanagement.dto.response.OrderResponse;
import com.shopmanagement.entity.*;
import com.shopmanagement.service.NotificationService;
import com.shopmanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Repair Orders", description = "Endpoints for managing repair orders including creation, updates, status tracking, and image uploads")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    
    private final OrderService orderService;
    private final NotificationService notificationService;
    
    @Operation(
            summary = "Get all repair orders",
            description = "Retrieve paginated list of repair orders with optional filters by status, date range, and search query"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully"
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RepairOrder> orders;
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            orders = orderService.searchOrders(search, pageable);
        } else if (status != null && startDate != null && endDate != null) {
            orders = orderService.getOrdersByStatusAndDateRange(status, startDate, endDate, pageable);
        } else if (status != null) {
            orders = orderService.getOrdersByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            orders = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        } else {
            orders = orderService.getAllOrders(pageable);
        }
        
        Page<OrderResponse> response = orders.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get repair order by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        RepairOrder order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(order)));
    }
    
    /**
     * Get repair orders for a specific customer
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RepairOrder> orders = orderService.getOrdersByCustomer(customerId, pageable);
        Page<OrderResponse> response = orders.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get repair orders assigned to a specific staff member
     */
    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RepairOrder> orders = orderService.getOrdersByStaff(staffId, pageable);
        Page<OrderResponse> response = orders.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(
            summary = "Create new repair order",
            description = "Create a new repair order with device details, customer information, and assigned staff"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Repair order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        RepairOrder order = convertToEntity(request);
        RepairOrder createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(convertToResponse(createdOrder), "Repair order created successfully"));
    }
    
    /**
     * Update an existing repair order
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        
        RepairOrder order = convertToEntity(request);
        RepairOrder updatedOrder = orderService.updateOrder(id, order);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(updatedOrder), "Repair order updated successfully"));
    }
    
    /**
     * Update repair order status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        
        RepairOrder updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(updatedOrder), "Order status updated successfully"));
    }
    
    /**
     * Delete a repair order
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Repair order deleted successfully"));
    }
    
    /**
     * Upload images for a repair order
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<OrderImageResponse>>> uploadOrderImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        
        List<OrderImage> images = orderService.uploadOrderImages(id, files);
        List<OrderImageResponse> response = images.stream()
                .map(this::convertToImageResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Images uploaded successfully"));
    }
    
    /**
     * Delete an order image
     */
    @DeleteMapping("/{orderId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOrderImage(
            @PathVariable Long orderId,
            @PathVariable Long imageId) {
        
        orderService.deleteOrderImage(orderId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
    }
    
    /**
     * Get order count by status
     */
    @GetMapping("/count/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Long>> getOrderCountByStatus(@PathVariable OrderStatus status) {
        Long count = orderService.getOrderCountByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * Send notification to customer about order status
     */
    @PostMapping("/{id}/notify")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> sendOrderNotification(
            @PathVariable Long id,
            @RequestParam(defaultValue = "email") String channel) {
        
        notificationService.sendOrderNotification(id, channel);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification sent successfully"));
    }
    
    // Helper methods for DTO conversion
    
    private OrderResponse convertToResponse(RepairOrder order) {
        OrderResponse.CustomerSummary customerSummary = OrderResponse.CustomerSummary.builder()
                .id(order.getCustomer().getId())
                .name(order.getCustomer().getName())
                .phoneNumber(order.getCustomer().getPhoneNumber())
                .email(order.getCustomer().getEmail())
                .build();
        
        OrderResponse.StaffSummary staffSummary = null;
        if (order.getAssignedStaff() != null) {
            staffSummary = OrderResponse.StaffSummary.builder()
                    .id(order.getAssignedStaff().getId())
                    .name(order.getAssignedStaff().getName())
                    .phoneNumber(order.getAssignedStaff().getPhoneNumber())
                    .build();
        }
        
        List<OrderImageResponse> imageResponses = order.getImages().stream()
                .map(this::convertToImageResponse)
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .customer(customerSummary)
                .deviceModel(order.getDeviceModel())
                .problemDescription(order.getProblemDescription())
                .estimatedPrice(order.getEstimatedPrice())
                .paidAmount(order.getPaidAmount())
                .lockCode(order.getLockCode())
                .repairDate(order.getRepairDate())
                .accessories(order.getAccessories())
                .serialNumber(order.getSerialNumber())
                .assignedStaff(staffSummary)
                .status(order.getStatus())
                .cashbackEnabled(order.getCashbackEnabled())
                .cashbackAmount(order.getCashbackAmount())
                .warrantyDays(order.getWarrantyDays())
                .expenses(order.getExpenses())
                .images(imageResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    private OrderImageResponse convertToImageResponse(OrderImage image) {
        return OrderImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .description(image.getDescription())
                .createdAt(image.getCreatedAt())
                .build();
    }
    
    private RepairOrder convertToEntity(OrderRequest request) {
        RepairOrder order = new RepairOrder();
        
        // Set customer (will be validated in service)
        Customer customer = new Customer();
        customer.setId(request.getCustomerId());
        order.setCustomer(customer);
        
        // Set staff if provided
        if (request.getAssignedStaffId() != null) {
            Staff staff = new Staff();
            staff.setId(request.getAssignedStaffId());
            order.setAssignedStaff(staff);
        }
        
        order.setDeviceModel(request.getDeviceModel());
        order.setProblemDescription(request.getProblemDescription());
        order.setEstimatedPrice(request.getEstimatedPrice());
        order.setPaidAmount(request.getPaidAmount());
        order.setLockCode(request.getLockCode());
        order.setRepairDate(request.getRepairDate());
        order.setAccessories(request.getAccessories());
        order.setSerialNumber(request.getSerialNumber());
        order.setStatus(request.getStatus());
        order.setCashbackEnabled(request.getCashbackEnabled());
        order.setWarrantyDays(request.getWarrantyDays());
        order.setExpenses(request.getExpenses());
        
        return order;
    }
}
