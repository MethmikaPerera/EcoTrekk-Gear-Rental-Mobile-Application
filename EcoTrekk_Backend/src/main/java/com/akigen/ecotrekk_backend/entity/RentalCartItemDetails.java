package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "rental_cart_item_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalCartItemDetails {

    @Id
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", foreignKey = @ForeignKey(name = "fk_rental_cart_item"))
    private CartItem cartItem;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

}
