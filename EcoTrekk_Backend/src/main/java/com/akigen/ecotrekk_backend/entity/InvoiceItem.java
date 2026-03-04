package com.akigen.ecotrekk_backend.entity;

import com.akigen.ecotrekk_backend.enums.CartItemType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invoice_items_invoice"))
    private Invoice invoice;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gear_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invoice_items_gear"))
    private Gear gear;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private CartItemType itemType;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Builder.Default
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "deposit_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositTotal = BigDecimal.ZERO;

    // Subtype links
    @OneToOne(mappedBy = "invoiceItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private RentalInvoiceItemDetails rentalDetails;

    @OneToOne(mappedBy = "invoiceItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private SaleInvoiceItemDetails saleDetails;

}
