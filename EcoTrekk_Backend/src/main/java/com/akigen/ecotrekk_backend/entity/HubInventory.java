package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hub_inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_hub"))
    private Hub hub;

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
