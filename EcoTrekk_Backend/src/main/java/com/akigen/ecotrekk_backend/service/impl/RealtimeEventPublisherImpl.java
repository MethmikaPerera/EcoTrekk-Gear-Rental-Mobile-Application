package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.websocket.RealtimeEvent;
import com.akigen.ecotrekk_backend.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of RealtimeEventPublisher that broadcasts WebSocket events
 */
@Service
@RequiredArgsConstructor
public class RealtimeEventPublisherImpl implements RealtimeEventPublisher {
    
    private final SimpMessagingTemplate messagingTemplate;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    
    @Override
    public void publishEvent(String topic, RealtimeEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now().format(dateTimeFormatter));
        }
        messagingTemplate.convertAndSend("/topic/" + topic, event);
    }
    
    @Override
    public void publishOrderEvent(String eventType, Object orderData) {
        RealtimeEvent event = new RealtimeEvent(
            eventType,
            LocalDateTime.now().format(dateTimeFormatter),
            orderData
        );
        publishEvent("orders", event);
    }
    
    @Override
    public void publishGearEvent(String eventType, Object gearData) {
        RealtimeEvent event = new RealtimeEvent(
            eventType,
            LocalDateTime.now().format(dateTimeFormatter),
            gearData
        );
        publishEvent("gears", event);
    }
    
    @Override
    public void publishUserEvent(String eventType, Object userData) {
        RealtimeEvent event = new RealtimeEvent(
            eventType,
            LocalDateTime.now().format(dateTimeFormatter),
            userData
        );
        publishEvent("users", event);
    }
    
    @Override
    public void publishHubEvent(String eventType, Object hubData) {
        RealtimeEvent event = new RealtimeEvent(
            eventType,
            LocalDateTime.now().format(dateTimeFormatter),
            hubData
        );
        publishEvent("hubs", event);
    }
    
    @Override
    public void publishMetricsEvent(Object metricsData) {
        RealtimeEvent event = new RealtimeEvent(
            "METRICS_UPDATED",
            LocalDateTime.now().format(dateTimeFormatter),
            metricsData
        );
        publishEvent("metrics", event);
    }
}
