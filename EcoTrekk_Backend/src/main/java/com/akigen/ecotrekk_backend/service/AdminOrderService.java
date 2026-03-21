package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.AdminOrderStatusUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderPageResponseDTO;

import java.time.LocalDate;

public interface AdminOrderService {
    AdminOrderPageResponseDTO getOrders(int page, int size, String status, String search, LocalDate startDate, LocalDate endDate);

    AdminOrderDTO getOrderById(String id);

    void updateOrderStatus(String id, AdminOrderStatusUpdateRequestDTO request);
}
