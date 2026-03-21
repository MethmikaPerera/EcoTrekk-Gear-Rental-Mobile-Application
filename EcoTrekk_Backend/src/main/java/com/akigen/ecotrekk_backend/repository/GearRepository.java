package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Gear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GearRepository extends JpaRepository<Gear, Long> {
    List<Gear> findByActiveTrue();

    List<Gear> findByCategoryIdAndActiveTrue(Long categoryId);

    long countByCategoryId(Long categoryId);
}
