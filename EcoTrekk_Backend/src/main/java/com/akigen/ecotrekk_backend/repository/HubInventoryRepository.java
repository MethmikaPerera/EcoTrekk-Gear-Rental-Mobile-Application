package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.entity.HubInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HubInventoryRepository extends JpaRepository<HubInventory, Long> {
    Optional<HubInventory> findByHubIdAndGearId(Long hubId, Long gearId);

    List<HubInventory> findByHubId(Long hubId);

    List<HubInventory> findByGearId(Long gearId);

    long countByAvailableQtyLessThanEqual(int threshold);

    long countByHubIdInAndAvailableQtyLessThanEqual(List<Long> hubIds, int threshold);

    @Query("""
            SELECT new com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO(
                hi.id,
                g.id,
                g.name,
                c.name,
                g.description,
                g.dailyRentPrice,
                hi.availableQty,
                CASE WHEN g.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END,
                h.id,
                h.name,
                g.updatedAt
            )
            FROM HubInventory hi
            JOIN hi.gear g
            JOIN g.category c
            JOIN hi.hub h
            WHERE (:search IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:category IS NULL OR LOWER(c.name) = LOWER(:category))
              AND (:status IS NULL OR (:status = 'ACTIVE' AND g.active = true) OR (:status = 'INACTIVE' AND g.active = false))
              AND (:hub IS NULL OR LOWER(h.name) = LOWER(:hub))
            ORDER BY g.updatedAt DESC
            """)
    Page<GearAdminListItemDTO> searchAdminGearInventory(
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") String status,
            @Param("hub") String hub,
            Pageable pageable
    );
}
