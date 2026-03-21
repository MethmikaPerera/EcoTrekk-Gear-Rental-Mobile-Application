package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gears")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gear extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gears_category"))
    private GearCategory category;

    @Column(name = "name", nullable = false, length = 140)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "daily_rent_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRentPrice;

    @Builder.Default
    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
