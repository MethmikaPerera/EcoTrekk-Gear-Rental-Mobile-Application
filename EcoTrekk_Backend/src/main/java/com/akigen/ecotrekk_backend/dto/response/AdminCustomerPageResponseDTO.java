package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerPageResponseDTO {
    private List<AdminCustomerSummaryDTO> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
}