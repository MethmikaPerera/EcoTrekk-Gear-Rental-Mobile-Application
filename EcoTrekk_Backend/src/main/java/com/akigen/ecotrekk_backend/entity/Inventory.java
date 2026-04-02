package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gear_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_gear"))
    private Gear gear;

    @Builder.Default
    @Column(name = "total_qty", nullable = false)
    private int totalQty = 0;

    @Builder.Default
    @Column(name = "available_qty", nullable = false)
    private int availableQty = 0;

}
