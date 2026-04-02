package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.response.NotificationDTO;
import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.enums.NotificationType;
import java.util.List;

public interface NotificationService {
    
    /**
     * Send order status change notification to customer
     */
    void sendOrderStatusNotification(Invoice invoice, String newStatus);
    
    /**
     * Send order approval notification
     */
    void sendOrderApprovalNotification(Invoice invoice);
    
    /**
     * Send generic notification
     */
    void sendNotification(Long customerId, String title, String body, 
                         NotificationType type, Long invoiceId);
    
    /**
     * Get notifications for a customer
     */
    List<NotificationDTO> getCustomerNotifications(Long customerId);
    
    /**
     * Mark notification as read
     */
    void markNotificationAsRead(Long notificationId);
}
