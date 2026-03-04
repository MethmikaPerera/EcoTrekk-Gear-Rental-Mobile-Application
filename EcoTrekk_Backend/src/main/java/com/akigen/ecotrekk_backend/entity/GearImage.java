package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gear_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gear_id", nullable = false, foreignKey = @ForeignKey(name = "fk_gear_images_gear"))
    private Gear gear;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

}
