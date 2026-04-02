package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    @Query("SELECT ii FROM InvoiceItem ii JOIN FETCH ii.gear WHERE ii.invoice.id = :invoiceId")
    List<InvoiceItem> findByInvoiceId(@Param("invoiceId") Long invoiceId);

    List<InvoiceItem> findByInvoiceIdIn(List<Long> invoiceIds);
}
