package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_device_tokens", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_fcm_token", columnList = "fcm_token"),
    @Index(name = "idx_device_id", columnList = "device_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_customer_device", columnNames = {"customer_id", "device_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeviceToken extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_device_tokens_customer"))
    private Customer customer;
    
    @Column(name = "device_id", nullable = false, length = 128)
    private String deviceId;  // Android ANDROID_ID or hardware ID
    
    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;  // Firebase Cloud Messaging token
    
    @Column(name = "device_model", length = 128)
    private String deviceModel;  // e.g., "Pixel 7", "Samsung Galaxy S21"
    
    @Column(name = "os_version", length = 64)
    private String osVersion;  // e.g., "Android 13 (API 33)"
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
