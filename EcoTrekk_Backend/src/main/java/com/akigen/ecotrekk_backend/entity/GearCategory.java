package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gear_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearCategory extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 80, unique = true)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

}
