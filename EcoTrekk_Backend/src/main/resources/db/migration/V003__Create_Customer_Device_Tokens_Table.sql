-- Migration: Create customer_device_tokens table for FCM token storage
-- This table stores Firebase Cloud Messaging tokens for each customer device
-- enabling push notifications to specific devices

CREATE TABLE IF NOT EXISTS customer_device_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    fcm_token VARCHAR(512) NOT NULL,
    device_model VARCHAR(128),
    os_version VARCHAR(64),
    is_active BOOLEAN DEFAULT true,
    last_updated DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_customer_device (customer_id, device_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_fcm_token (fcm_token),
    INDEX idx_device_id (device_id)
);
