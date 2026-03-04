package com.akigen.ecotrekk_backend.entity;

import com.akigen.ecotrekk_backend.enums.GearType;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "gear_type", nullable = false, length = 20)
    private GearType gearType;

    @Column(name = "name", nullable = false, length = 140)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // subtype links
    @OneToOne(mappedBy = "gear", cascade = CascadeType.ALL, orphanRemoval = true)
    private RentalGearDetails rentalDetails;

    @OneToOne(mappedBy = "gear", cascade = CascadeType.ALL, orphanRemoval = true)
    private SaleGearDetails saleDetails;
}
