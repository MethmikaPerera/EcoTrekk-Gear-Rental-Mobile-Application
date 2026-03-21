package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.websocket.RealtimeEvent;

/**
 * Service for publishing real-time WebSocket events to connected clients
 */
public interface RealtimeEventPublisher {
    
    /**
     * Publish a real-time event to all connected clients
     */
    void publishEvent(String topic, RealtimeEvent event);
    
    /**
     * Publish an order-related event
     */
    void publishOrderEvent(String eventType, Object orderData);
    
    /**
     * Publish a gear-related event
     */
    void publishGearEvent(String eventType, Object gearData);
    
    /**
     * Publish a user-related event
     */
    void publishUserEvent(String eventType, Object userData);
    
    /**
     * Publish a hub-related event
     */
    void publishHubEvent(String eventType, Object hubData);
    
    /**
     * Publish a metrics update event
     */
    void publishMetricsEvent(Object metricsData);
}
