package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.HubInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HubInventoryRepository extends JpaRepository<HubInventory, Long> {
    Optional<HubInventory> findByHubIdAndGearId(Long hubId, Long gearId);

    List<HubInventory> findByHubId(Long hubId);

    List<HubInventory> findByGearId(Long gearId);
}
