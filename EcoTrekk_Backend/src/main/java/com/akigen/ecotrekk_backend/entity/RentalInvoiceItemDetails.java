package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rental_invoice_item_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalInvoiceItemDetails {

    @Id
    @Column(name = "invoice_item_id")
    private Long invoiceItemId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_item_id",
            foreignKey = @ForeignKey(name = "fk_rental_invoice_item"))
    private InvoiceItem invoiceItem;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "daily_rent_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRentPrice;

    @Builder.Default
    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

}
