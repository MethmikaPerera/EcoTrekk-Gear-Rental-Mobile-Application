package com.akigen.ecotrekk_backend.controller.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket controller for handling real-time connections and subscriptions
 */
@Controller
@Slf4j
public class RealtimeController {
    
    /**
     * Endpoint for clients to subscribe to real-time updates
     * Clients can get live notifications on various topics:
     * - /topic/orders
     * - /topic/gears
     * - /topic/users
     * - /topic/hubs
     * - /topic/metrics
     */
    @MessageMapping("/subscribe")
    @SendTo("/topic/connection")
    public String handleSubscribe(String message) {
        log.info("Client subscribed to real-time updates: {}", message);
        return "Connection established";
    }
}
