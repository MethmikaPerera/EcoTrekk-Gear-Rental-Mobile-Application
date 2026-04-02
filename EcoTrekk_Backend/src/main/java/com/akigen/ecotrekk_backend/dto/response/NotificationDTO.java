package com.akigen.ecotrekk_backend.dto.response;

import com.akigen.ecotrekk_backend.enums.NotificationType;
import com.akigen.ecotrekk_backend.enums.NotificationChannel;
import com.akigen.ecotrekk_backend.enums.NotificationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String body;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
