package com.akigen.ecotrekk_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerOrderHistoryResponseDTO {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String startDate;
    private String endDate;
    private String pickupHubName;
    private Double pickupHubLatitude;
    private Double pickupHubLongitude;
    private Double subtotal;
    private Double refundableDeposit;
    private Double totalAmount;
    private String status;
    private Long createdAt;
    private List<CustomerOrderItemResponseDTO> items;
}
