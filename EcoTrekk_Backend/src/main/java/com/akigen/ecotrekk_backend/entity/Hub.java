package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hubs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hub extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "address_line1", nullable = false, length = 160)
    private String addressLine1;

    @Column(name = "address_line2", length = 160)
    private String addressLine2;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "district", length = 80)
    private String district;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "map_location_url", length = 500)
    private String mapLocationUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

}
