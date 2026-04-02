package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.response.NotificationDTO;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.entity.Notification;
import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.entity.CustomerDeviceToken;
import com.akigen.ecotrekk_backend.enums.NotificationType;
import com.akigen.ecotrekk_backend.enums.NotificationChannel;
import com.akigen.ecotrekk_backend.enums.NotificationStatus;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceRepository;
import com.akigen.ecotrekk_backend.repository.NotificationRepository;
import com.akigen.ecotrekk_backend.repository.CustomerDeviceTokenRepository;
import com.akigen.ecotrekk_backend.service.NotificationService;
import com.akigen.ecotrekk_backend.service.FcmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final CustomerDeviceTokenRepository deviceTokenRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final FcmService fcmService;
    
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   CustomerDeviceTokenRepository deviceTokenRepository,
                                   CustomerRepository customerRepository,
                                   InvoiceRepository invoiceRepository,
                                   FcmService fcmService) {
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.fcmService = fcmService;
    }
    
    @Override
    public void sendOrderApprovalNotification(Invoice invoice) {
        String title = "Order Approved";
        String body = String.format("Your order #%s has been approved! You can pick it up from the hub.", 
                                    invoice.getInvoiceCode());
        sendNotification(invoice.getCustomer().getId(), title, body, 
                        NotificationType.ORDER_STATUS, invoice.getId());
    }
    
    @Override
    public void sendOrderStatusNotification(Invoice invoice, String newStatus) {
        String status = newStatus.toLowerCase();
        String title = "Order Status Updated";
        String body = "";
        NotificationType type = NotificationType.ORDER_STATUS;
        
        switch(status) {
            case "ready":
                body = String.format("Your order #%s is ready for pickup!", invoice.getInvoiceCode());
                break;
            case "picked_up":
                body = String.format("Your order #%s has been picked up.", invoice.getInvoiceCode());
                break;
            case "delivered":
                body = String.format("Your order #%s has been delivered. Enjoy!", invoice.getInvoiceCode());
                break;
            case "completed":
                body = String.format("Your order #%s rental has been completed.", invoice.getInvoiceCode());
                break;
            case "cancelled":
                body = String.format("Your order #%s has been cancelled.", invoice.getInvoiceCode());
                type = NotificationType.ORDER_STATUS;
                break;
            default:
                body = String.format("Your order #%s status has been updated to %s.", 
                                    invoice.getInvoiceCode(), newStatus);
        }
        
        sendNotification(invoice.getCustomer().getId(), title, body, type, invoice.getId());
    }
    
    @Override
    public void sendNotification(Long customerId, String title, String body, 
                                 NotificationType type, Long invoiceId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

            Invoice invoice = null;
            if (invoiceId != null) {
                invoice = invoiceRepository.findById(invoiceId).orElse(null);
            }

            // Create notification record for audit trail
            Notification notification = new Notification();
            notification.setCustomer(customer);
            notification.setInvoice(invoice);
            notification.setTitle(title);
            notification.setBody(body);
            notification.setType(type);
            notification.setChannel(NotificationChannel.FCM);
            notification.setStatus(NotificationStatus.QUEUED);
            
            notificationRepository.save(notification);
            log.info("Notification record created for customer: {}", customerId);
            
            // Get active device tokens for this customer
            List<CustomerDeviceToken> deviceTokens = 
                    deviceTokenRepository.findByCustomerIdAndIsActiveTrue(customerId);
            
            if (deviceTokens.isEmpty()) {
                log.warn("No active device tokens found for customer: {}", customerId);
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
                return;
            }
            
            // Send to all active devices
            for (CustomerDeviceToken token : deviceTokens) {
                try {
                    String targetScreen = "order_history";  // Default deep-link target
                    fcmService.sendNotification(token.getFcmToken(), title, body, targetScreen);
                    notification.setStatus(NotificationStatus.SENT);
                    log.info("FCM notification sent to device: {} for customer: {}", 
                            token.getDeviceId(), customerId);
                } catch (Exception e) {
                    log.error("Failed to send FCM to device: {} ", token.getDeviceId(), e);
                    notification.setStatus(NotificationStatus.FAILED);
                }
            }
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Error in sendNotification for customer: {}", customerId, e);
        }
    }
    
    @Override
    public List<NotificationDTO> getCustomerNotifications(Long customerId) {
        List<Notification> notifications = notificationRepository.findByCustomerId(customerId);
        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);
        });
    }
    
    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .createdAt(java.time.LocalDateTime.ofInstant(notification.getCreatedAt(), java.time.ZoneId.systemDefault()))
                .build();
    }
}
