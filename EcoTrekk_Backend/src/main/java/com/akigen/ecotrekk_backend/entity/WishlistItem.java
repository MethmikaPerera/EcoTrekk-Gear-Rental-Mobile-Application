package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "wishlist_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wishlist_items_wishlist"))
    private Wishlist wishlist;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gear_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wishlist_items_gear"))
    private Gear gear;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}
