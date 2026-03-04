package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Notification;
import com.akigen.ecotrekk_backend.enums.NotificationStatus;
import com.akigen.ecotrekk_backend.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerId(Long customerId);

    List<Notification> findByInvoiceId(Long invoiceId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByType(NotificationType type);

    List<Notification> findByCustomerIdAndStatus(Long customerId, NotificationStatus status);
}
