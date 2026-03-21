package com.akigen.ecotrekk_backend.dto.websocket;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Base class for all WebSocket events
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeEvent {
    private String eventType;  // ORDER_UPDATED, GEAR_UPDATED, USER_UPDATED, etc.
    private String timestamp;
    private Object data;
}
