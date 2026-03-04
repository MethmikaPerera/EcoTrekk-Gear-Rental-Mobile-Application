package com.akigen.ecotrekk_backend.entity;

import com.akigen.ecotrekk_backend.enums.CartItemType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
    private Cart cart;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_hub"))
    private Hub hub;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gear_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_gear"))
    private Gear gear;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private CartItemType itemType;

    @Column(name = "qty", nullable = false)
    private int qty;

    @OneToOne(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private RentalCartItemDetails rentalDetails;

    @OneToOne(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private SaleCartItemDetails saleDetails;

}
