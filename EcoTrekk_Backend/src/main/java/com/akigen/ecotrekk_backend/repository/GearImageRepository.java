package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.GearImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GearImageRepository extends JpaRepository<GearImage, Long> {
    List<GearImage> findByGearIdOrderBySortOrderAsc(Long gearId);

    List<GearImage> findByGearIdAndPrimaryTrue(Long gearId);
}
