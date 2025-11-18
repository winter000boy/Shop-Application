package com.shopmanagement.service;

import com.shopmanagement.entity.Notification;
import com.shopmanagement.entity.RepairOrder;
import com.shopmanagement.entity.Shop;
import com.shopmanagement.exception.ResourceNotFoundException;
import com.shopmanagement.repository.NotificationRepository;
import com.shopmanagement.repository.RepairOrderRepository;
import com.shopmanagement.repository.ShopRepository;
import com.shopmanagement.security.ShopContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing notifications and sending messages to customers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final ShopRepository shopRepository;
    private final EmailService emailService;
    
    /**
     * Get all notifications for the current shop
     */
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return notificationRepository.findByShopIdOrderByCreatedAtDesc(shopId, pageable);
    }
    
    /**
     * Get unread notifications for the current shop
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(Pageable pageable) {
        Long shopId = ShopContext.getCurrentShopId();
        return notificationRepository.findByShopIdAndIsReadOrderByCreatedAtDesc(shopId, false, pageable);
    }
    
    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        Long shopId = ShopContext.getCurrentShopId();
        return notificationRepository.countByShopIdAndIsRead(shopId, false);
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        // Verify notification belongs to shop
        if (!notification.getShopId().equals(shopId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
    
    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead() {
        Long shopId = ShopContext.getCurrentShopId();
        Page<Notification> unreadNotifications = notificationRepository
                .findByShopIdAndIsReadOrderByCreatedAtDesc(shopId, false, Pageable.unpaged());
        
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Create a notification
     */
    @Transactional
    public Notification createNotification(String title, String message, String type, String referenceId) {
        Long shopId = ShopContext.getCurrentShopId();
        
        Notification notification = new Notification();
        notification.setShopId(shopId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Send order notification to customer via email
     */
    @Transactional
    public void sendOrderNotification(Long orderId, String channel) {
        Long shopId = ShopContext.getCurrentShopId();
        
        // Get order details
        RepairOrder order = repairOrderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair order not found with id: " + orderId));
        
        // Get shop details
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        
        String customerEmail = order.getCustomer().getEmail();
        String customerName = order.getCustomer().getName();
        String deviceModel = order.getDeviceModel();
        String status = order.getStatus().toString();
        
        try {
            if ("email".equalsIgnoreCase(channel)) {
                if (customerEmail == null || customerEmail.trim().isEmpty()) {
                    throw new IllegalArgumentException("Customer email is not available");
                }
                
                // Send email based on order status
                if ("COMPLETED".equals(status)) {
                    emailService.sendOrderCompletionEmail(
                            customerEmail, 
                            customerName, 
                            deviceModel, 
                            shop.getShopName(), 
                            shop.getPhoneNumber()
                    );
                } else {
                    emailService.sendOrderStatusEmail(
                            customerEmail, 
                            customerName, 
                            deviceModel, 
                            status, 
                            shop.getShopName()
                    );
                }
                
                // Create notification record
                createNotification(
                        "Email Sent",
                        "Order status email sent to " + customerName,
                        "ORDER_EMAIL",
                        orderId.toString()
                );
                
                log.info("Order notification sent via email for order: {}", orderId);
                
            } else if ("whatsapp".equalsIgnoreCase(channel)) {
                // WhatsApp integration would go here
                // For now, just log and create notification
                String message = String.format(
                        "Order Update: Your %s repair status is now %s. - %s",
                        deviceModel, status, shop.getShopName()
                );
                
                log.info("WhatsApp message would be sent to {}: {}", 
                        order.getCustomer().getPhoneNumber(), message);
                
                // Create notification record
                createNotification(
                        "WhatsApp Message",
                        "Order status WhatsApp message sent to " + customerName,
                        "ORDER_WHATSAPP",
                        orderId.toString()
                );
                
            } else {
                throw new IllegalArgumentException("Invalid notification channel: " + channel);
            }
            
        } catch (Exception e) {
            log.error("Failed to send order notification for order: {}", orderId, e);
            
            // Create error notification
            createNotification(
                    "Notification Failed",
                    "Failed to send " + channel + " notification: " + e.getMessage(),
                    "ERROR",
                    orderId.toString()
            );
            
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }
    
    /**
     * Send order notification via both email and WhatsApp
     */
    @Transactional
    public void sendOrderNotificationAll(Long orderId) {
        try {
            sendOrderNotification(orderId, "email");
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
        
        try {
            sendOrderNotification(orderId, "whatsapp");
        } catch (Exception e) {
            log.error("Failed to send WhatsApp notification", e);
        }
    }
    
    /**
     * Send invoice notification to customer
     */
    @Transactional
    public void sendInvoiceNotification(Long invoiceId, String channel) {
        // This method is called from InvoiceController
        // For WhatsApp, we would integrate with WhatsApp Business API
        // For now, just log and create notification
        
        log.info("Invoice notification would be sent via {} for invoice: {}", channel, invoiceId);
        
        createNotification(
                "Invoice Sent",
                "Invoice sent via " + channel,
                "INVOICE_" + channel.toUpperCase(),
                invoiceId.toString()
        );
    }
}
