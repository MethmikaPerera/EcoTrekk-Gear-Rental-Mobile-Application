package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.InvoiceItem;
import com.akigen.ecotrekk_backend.enums.CartItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    List<InvoiceItem> findByInvoiceIdAndItemType(Long invoiceId, CartItemType itemType);
}
