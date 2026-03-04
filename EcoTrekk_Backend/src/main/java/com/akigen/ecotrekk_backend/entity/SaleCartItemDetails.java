package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sale_cart_item_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCartItemDetails {

    @Id
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", foreignKey = @ForeignKey(name = "fk_sale_cart_item"))
    private CartItem cartItem;

}
