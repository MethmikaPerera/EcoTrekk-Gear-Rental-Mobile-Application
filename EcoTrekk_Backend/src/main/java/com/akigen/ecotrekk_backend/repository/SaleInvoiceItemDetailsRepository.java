package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.SaleInvoiceItemDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleInvoiceItemDetailsRepository extends JpaRepository<SaleInvoiceItemDetails, Long> {
}
