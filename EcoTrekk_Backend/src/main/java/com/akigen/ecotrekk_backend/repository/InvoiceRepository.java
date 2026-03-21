package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.enums.InvoiceStatus;
import com.akigen.ecotrekk_backend.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.Instant;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status);

    long countByStatus(InvoiceStatus status);

    long countByStatusAndHubIdIn(InvoiceStatus status, List<Long> hubIds);

    List<Invoice> findByCreatedAtBetween(Instant startInclusive, Instant endExclusive);

    List<Invoice> findByHubIdInAndCreatedAtBetween(List<Long> hubIds, Instant startInclusive, Instant endExclusive);

        @Query("""
            SELECT i FROM Invoice i
            JOIN i.customer c
            WHERE (:status IS NULL OR i.status = :status)
              AND (:search IS NULL
                 OR LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:startDate IS NULL OR EXISTS (
                  SELECT ii.id FROM InvoiceItem ii
                  WHERE ii.invoice = i AND ii.endDate >= :startDate
              ))
              AND (:endDate IS NULL OR EXISTS (
                  SELECT ii.id FROM InvoiceItem ii
                  WHERE ii.invoice = i AND ii.startDate <= :endDate
              ))
            """)
        Page<Invoice> searchAdminOrders(
            @Param("status") InvoiceStatus status,
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
        );
}
