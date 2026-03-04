package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_gear_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleGearDetails {

    @Id
    @Column(name = "gear_id")
    private Long gearId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gear_id", foreignKey = @ForeignKey(name = "fk_sale_gear_details"))
    private Gear gear;

    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

}
