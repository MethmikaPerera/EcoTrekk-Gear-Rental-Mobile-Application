package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.enums.GearType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GearRepository extends JpaRepository<Gear, Long> {
    List<Gear> findByActiveTrue();

    List<Gear> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Gear> findByGearTypeAndActiveTrue(GearType gearType);

    List<Gear> findByCategoryIdAndGearTypeAndActiveTrue(Long categoryId, GearType gearType);
}
