package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.enums.OrderStatus;
import com.akigen.ecotrekk_backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByStatus(OrderStatus status);

    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Invoice> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
}
