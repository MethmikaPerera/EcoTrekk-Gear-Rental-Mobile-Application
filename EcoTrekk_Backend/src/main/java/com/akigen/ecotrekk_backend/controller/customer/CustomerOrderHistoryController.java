package com.akigen.ecotrekk_backend.controller.customer;

import com.akigen.ecotrekk_backend.dto.response.CustomerOrderHistoryResponseDTO;
import com.akigen.ecotrekk_backend.service.CustomerOrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderHistoryController {

    private final CustomerOrderHistoryService customerOrderHistoryService;

    @GetMapping("/{customerId}")
    public ResponseEntity<List<CustomerOrderHistoryResponseDTO>> getCustomerOrders(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerOrderHistoryService.getCustomerOrderHistory(customerId));
    }
}
