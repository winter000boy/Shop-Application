package com.shopmanagement.controller;

import com.shopmanagement.dto.request.InvoiceRequest;
import com.shopmanagement.dto.response.ApiResponse;
import com.shopmanagement.dto.response.InvoiceResponse;
import com.shopmanagement.entity.Invoice;
import com.shopmanagement.service.EmailService;
import com.shopmanagement.service.InvoiceService;
import com.shopmanagement.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Endpoints for generating, managing, and sending invoices")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    /**
     * Get all invoices with pagination and optional filters
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getAllInvoices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Boolean sent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Invoice> invoices;
        
        // Apply filters
        if (startDate != null && endDate != null) {
            invoices = invoiceService.getInvoicesByDateRange(startDate, endDate, pageable);
        } else if (sent != null) {
            invoices = invoiceService.getInvoicesBySentStatus(sent, pageable);
        } else {
            invoices = invoiceService.getAllInvoices(pageable);
        }
        
        Page<InvoiceResponse> response = invoices.map(this::convertToResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get invoice by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(invoice)));
    }
    
    /**
     * Get invoice by invoice number
     */
    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(invoice)));
    }
    
    /**
     * Get invoice by repair order ID
     */
    @GetMapping("/order/{repairOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByRepairOrderId(@PathVariable Long repairOrderId) {
        Invoice invoice = invoiceService.getInvoiceByRepairOrderId(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(invoice)));
    }
    
    /**
     * Create a new invoice
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        Invoice invoice;
        
        if (request.getRepairOrderId() != null) {
            // Create invoice from repair order
            invoice = invoiceService.createInvoice(
                    request.getRepairOrderId(),
                    request.getItems(),
                    request.getTaxAmount()
            );
        } else {
            // Create standalone invoice
            invoice = invoiceService.createStandaloneInvoice(
                    request.getItems(),
                    request.getSubtotal(),
                    request.getTaxAmount()
            );
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(convertToResponse(invoice), "Invoice created successfully"));
    }
    
    /**
     * Generate and download invoice PDF
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);
        Invoice invoice = invoiceService.getInvoiceById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", invoice.getInvoiceNumber() + ".pdf");
        headers.setContentLength(pdfBytes.length);
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Send invoice via email or WhatsApp
     */
    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendInvoice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "email") String channel) {
        
        Invoice invoice = invoiceService.getInvoiceById(id);
        
        // Generate PDF if not already generated
        if (invoice.getPdfUrl() == null || invoice.getPdfUrl().isEmpty()) {
            invoiceService.generateInvoicePdf(id);
            invoice = invoiceService.getInvoiceById(id); // Refresh to get PDF URL
        }
        
        // Send via specified channel
        if ("email".equalsIgnoreCase(channel)) {
            if (invoice.getRepairOrder() != null && 
                invoice.getRepairOrder().getCustomer() != null && 
                invoice.getRepairOrder().getCustomer().getEmail() != null) {
                
                String customerEmail = invoice.getRepairOrder().getCustomer().getEmail();
                String subject = "Invoice " + invoice.getInvoiceNumber();
                String body = "Dear Customer,\n\nPlease find attached your invoice.\n\nThank you for your business!";
                
                emailService.sendEmailWithAttachment(customerEmail, subject, body, invoice.getPdfUrl());
            }
        } else if ("whatsapp".equalsIgnoreCase(channel)) {
            if (invoice.getRepairOrder() != null) {
                notificationService.sendInvoiceNotification(id, "whatsapp");
            }
        }
        
        // Mark invoice as sent
        invoiceService.markInvoiceAsSent(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice sent successfully via " + channel));
    }
    
    /**
     * Delete an invoice
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted successfully"));
    }
    
    // Helper method for DTO conversion
    
    private InvoiceResponse convertToResponse(Invoice invoice) {
        InvoiceResponse.RepairOrderSummary orderSummary = null;
        
        if (invoice.getRepairOrder() != null) {
            InvoiceResponse.CustomerSummary customerSummary = InvoiceResponse.CustomerSummary.builder()
                    .id(invoice.getRepairOrder().getCustomer().getId())
                    .name(invoice.getRepairOrder().getCustomer().getName())
                    .phoneNumber(invoice.getRepairOrder().getCustomer().getPhoneNumber())
                    .build();
            
            orderSummary = InvoiceResponse.RepairOrderSummary.builder()
                    .id(invoice.getRepairOrder().getId())
                    .deviceModel(invoice.getRepairOrder().getDeviceModel())
                    .customer(customerSummary)
                    .build();
        }
        
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .repairOrder(orderSummary)
                .items(invoice.getItems())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .pdfUrl(invoice.getPdfUrl())
                .sent(invoice.getSent())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
