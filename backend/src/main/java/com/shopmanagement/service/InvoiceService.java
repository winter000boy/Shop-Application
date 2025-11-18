package com.shopmanagement.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.shopmanagement.entity.Invoice;
import com.shopmanagement.entity.RepairOrder;
import com.shopmanagement.entity.Shop;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.exception.ValidationException;
import com.shopmanagement.repository.InvoiceRepository;
import com.shopmanagement.repository.RepairOrderRepository;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final ShopRepository shopRepository;
    private final StorageService storageService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INVOICE_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Get all invoices for the current shop with pagination
     */
    @Transactional(readOnly = true)
    public Page<Invoice> getAllInvoices(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return invoiceRepository.findByShopId(shopId, pageable);
    }
    
    /**
     * Get invoices by date range
     */
    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return invoiceRepository.findByShopIdAndDateRange(shopId, startDate, endDate, pageable);
    }
    
    /**
     * Get invoices by sent status
     */
    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesBySentStatus(Boolean sent, Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return invoiceRepository.findByShopIdAndSent(shopId, sent, pageable);
    }
    
    /**
     * Get invoice by ID (with shop-level isolation)
     */
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long invoiceId) {
        Long shopId = ShopContext.getCurrentShopId();
        return invoiceRepository.findByIdAndShopId(invoiceId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
    }
    
    /**
     * Get invoice by invoice number
     */
    @Transactional(readOnly = true)
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        Long shopId = ShopContext.getCurrentShopId();
        return invoiceRepository.findByInvoiceNumberAndShopId(invoiceNumber, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
    }
    
    /**
     * Get invoice by repair order ID
     */
    @Transactional(readOnly = true)
    public Invoice getInvoiceByRepairOrderId(Long repairOrderId) {
        Long shopId = ShopContext.getCurrentShopId();
        return invoiceRepository.findByRepairOrderIdAndShopId(repairOrderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for repair order: " + repairOrderId));
    }
    
    /**
     * Create a new invoice from a repair order
     */
    @Transactional
    public Invoice createInvoice(Long repairOrderId, String items, BigDecimal taxAmount) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Validate repair order exists and belongs to shop
        RepairOrder repairOrder = repairOrderRepository.findByIdAndShopId(repairOrderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + repairOrderId));
        
        // Check if invoice already exists for this repair order
        if (invoiceRepository.findByRepairOrderIdAndShopId(repairOrderId, shopId).isPresent()) {
            throw new ValidationException("Invoice already exists for repair order: " + repairOrderId);
        }
        
        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setShopId(shopId);
        invoice.setRepairOrder(repairOrder);
        invoice.setInvoiceNumber(generateInvoiceNumber(shopId));
        invoice.setItems(items);
        
        // Calculate amounts
        BigDecimal subtotal = repairOrder.getPaidAmount();
        invoice.setSubtotal(subtotal);
        
        if (taxAmount != null && taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setTaxAmount(taxAmount);
            invoice.setTotalAmount(subtotal.add(taxAmount));
        } else {
            invoice.setTaxAmount(BigDecimal.ZERO);
            invoice.setTotalAmount(subtotal);
        }
        
        invoice.setSent(false);
        
        log.info("Creating invoice for repair order: {} in shop: {}", repairOrderId, shopId);
        return invoiceRepository.save(invoice);
    }
    
    /**
     * Create a standalone invoice (not linked to repair order)
     */
    @Transactional
    public Invoice createStandaloneInvoice(String items, BigDecimal subtotal, BigDecimal taxAmount) {
        Long shopId = ShopContext.getCurrentShopId();
        
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Subtotal must be greater than zero");
        }
        
        Invoice invoice = new Invoice();
        invoice.setShopId(shopId);
        invoice.setInvoiceNumber(generateInvoiceNumber(shopId));
        invoice.setItems(items);
        invoice.setSubtotal(subtotal);
        
        if (taxAmount != null && taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setTaxAmount(taxAmount);
            invoice.setTotalAmount(subtotal.add(taxAmount));
        } else {
            invoice.setTaxAmount(BigDecimal.ZERO);
            invoice.setTotalAmount(subtotal);
        }
        
        invoice.setSent(false);
        
        log.info("Creating standalone invoice in shop: {}", shopId);
        return invoiceRepository.save(invoice);
    }
    
    /**
     * Generate PDF for an invoice
     */
    @Transactional
    public byte[] generateInvoicePdf(Long invoiceId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Invoice invoice = invoiceRepository.findByIdAndShopId(invoiceId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Add shop header
            document.add(new Paragraph(shop.getShopName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph(shop.getAddress())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
            
            if (shop.getPhoneNumber() != null) {
                document.add(new Paragraph("Phone: " + shop.getPhoneNumber())
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            
            if (shop.getEmail() != null) {
                document.add(new Paragraph("Email: " + shop.getEmail())
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            
            if (shop.getGstNumber() != null && !shop.getGstNumber().isEmpty()) {
                document.add(new Paragraph("GST No: " + shop.getGstNumber())
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER));
            }
            
            document.add(new Paragraph("\n"));
            
            // Add invoice details
            document.add(new Paragraph("INVOICE")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("\n"));
            
            // Invoice info table
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            
            infoTable.addCell("Invoice Number:");
            infoTable.addCell(invoice.getInvoiceNumber());
            
            infoTable.addCell("Date:");
            infoTable.addCell(invoice.getCreatedAt().format(DATE_FORMATTER));
            
            if (invoice.getRepairOrder() != null) {
                RepairOrder order = invoice.getRepairOrder();
                infoTable.addCell("Customer:");
                infoTable.addCell(order.getCustomer().getName());
                
                if (order.getCustomer().getPhoneNumber() != null) {
                    infoTable.addCell("Phone:");
                    infoTable.addCell(order.getCustomer().getPhoneNumber());
                }
                
                infoTable.addCell("Device:");
                infoTable.addCell(order.getDeviceModel());
            }
            
            document.add(infoTable);
            document.add(new Paragraph("\n"));
            
            // Items/Services
            document.add(new Paragraph("Items/Services:")
                    .setFontSize(12)
                    .setBold());
            
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                document.add(new Paragraph(invoice.getItems())
                        .setFontSize(10));
            }
            
            document.add(new Paragraph("\n"));
            
            // Amount table
            Table amountTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
            amountTable.setWidth(UnitValue.createPercentValue(100));
            
            amountTable.addCell("Subtotal:");
            amountTable.addCell("₹" + invoice.getSubtotal().toString());
            
            if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                amountTable.addCell("Tax:");
                amountTable.addCell("₹" + invoice.getTaxAmount().toString());
            }
            
            amountTable.addCell(new Paragraph("Total:").setBold());
            amountTable.addCell(new Paragraph("₹" + invoice.getTotalAmount().toString()).setBold());
            
            document.add(amountTable);
            
            // Footer
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Thank you for your business!")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());
            
            document.close();
            
            byte[] pdfBytes = baos.toByteArray();
            
            // Upload PDF to storage and save URL
            String pdfUrl = storageService.uploadPdfBytes(pdfBytes, "invoices/" + invoice.getInvoiceNumber() + ".pdf");
            invoice.setPdfUrl(pdfUrl);
            invoiceRepository.save(invoice);
            
            log.info("Generated PDF for invoice: {} in shop: {}", invoiceId, shopId);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice: {}", invoiceId, e);
            throw new ValidationException("Failed to generate PDF: " + e.getMessage());
        }
    }
    
    /**
     * Mark invoice as sent
     */
    @Transactional
    public Invoice markInvoiceAsSent(Long invoiceId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Invoice invoice = invoiceRepository.findByIdAndShopId(invoiceId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        invoice.setSent(true);
        
        log.info("Marking invoice {} as sent in shop: {}", invoiceId, shopId);
        return invoiceRepository.save(invoice);
    }
    
    /**
     * Delete an invoice
     */
    @Transactional
    public void deleteInvoice(Long invoiceId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Invoice invoice = invoiceRepository.findByIdAndShopId(invoiceId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        // Delete PDF from storage if exists
        if (invoice.getPdfUrl() != null && !invoice.getPdfUrl().isEmpty()) {
            try {
                storageService.deleteFile(invoice.getPdfUrl());
            } catch (Exception e) {
                log.error("Failed to delete PDF: {}", invoice.getPdfUrl(), e);
            }
        }
        
        log.info("Deleting invoice: {} in shop: {}", invoiceId, shopId);
        invoiceRepository.delete(invoice);
    }
    
    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber(Long shopId) {
        String datePrefix = LocalDateTime.now().format(INVOICE_NUMBER_FORMATTER);
        
        // Get the latest invoice to determine sequence number
        Page<Invoice> latestInvoice = invoiceRepository.findLatestInvoiceByShopId(
                shopId, PageRequest.of(0, 1));
        
        int sequence = 1;
        if (!latestInvoice.isEmpty()) {
            Invoice latest = latestInvoice.getContent().get(0);
            String latestNumber = latest.getInvoiceNumber();
            
            // Extract sequence from latest invoice number if it has the same date prefix
            if (latestNumber.startsWith("INV-" + datePrefix)) {
                try {
                    String sequencePart = latestNumber.substring(latestNumber.lastIndexOf('-') + 1);
                    sequence = Integer.parseInt(sequencePart) + 1;
                } catch (Exception e) {
                    log.warn("Failed to parse sequence from invoice number: {}", latestNumber);
                }
            }
        }
        
        return String.format("INV-%s-%04d", datePrefix, sequence);
    }
}
