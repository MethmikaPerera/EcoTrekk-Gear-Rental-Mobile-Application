package com.akigen.ecotrekk_backend.entity;

import com.akigen.ecotrekk_backend.enums.NotificationChannel;
import com.akigen.ecotrekk_backend.enums.NotificationStatus;
import com.akigen.ecotrekk_backend.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_notifications_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", foreignKey = @ForeignKey(name = "fk_notifications_order"))
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NotificationStatus status;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}
