package com.akigen.ecotrekk_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {
    private String collectionHubName;
    private Long startDateUtc;  // milliseconds since epoch
    private Long endDateUtc;    // milliseconds since epoch
}
