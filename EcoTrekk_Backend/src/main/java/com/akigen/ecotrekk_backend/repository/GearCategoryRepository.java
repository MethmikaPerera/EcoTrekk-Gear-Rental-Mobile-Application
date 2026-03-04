package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.GearCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GearCategoryRepository extends JpaRepository<GearCategory, Long> {
    Optional<GearCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
