package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.response.CustomerOrderHistoryResponseDTO;

import java.util.List;

public interface CustomerOrderHistoryService {
    List<CustomerOrderHistoryResponseDTO> getCustomerOrderHistory(Long customerId);
}
