package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_invoice_item_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleInvoiceItemDetails {

    @Id
    @Column(name = "invoice_item_id")
    private Long invoiceItemId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_item_id",
            foreignKey = @ForeignKey(name = "fk_sale_invoice_item"))
    private InvoiceItem invoiceItem;

    @Column(name = "unit_sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitSalePrice;

}
