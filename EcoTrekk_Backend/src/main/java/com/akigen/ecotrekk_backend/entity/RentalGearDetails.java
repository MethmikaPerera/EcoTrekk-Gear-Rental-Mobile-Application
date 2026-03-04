package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rental_gear_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalGearDetails {
    @Id
    @Column(name="gear_id")
    private Long gearId;

    @MapsId
    @OneToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="gear_id", foreignKey = @ForeignKey(name="fk_rental_gear_details"))
    private Gear gear;

    @Column(name="daily_rent_price", nullable=false, precision=10, scale=2)
    private BigDecimal dailyRentPrice;

    @Builder.Default
    @Column(name="deposit_amount", nullable=false, precision=10, scale=2)
    private BigDecimal depositAmount = BigDecimal.ZERO;
}
