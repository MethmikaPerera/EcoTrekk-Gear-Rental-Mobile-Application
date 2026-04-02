package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO;
import com.akigen.ecotrekk_backend.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByGearId(Long gearId);

    long countByAvailableQtyLessThanEqual(int threshold);

    @Query("""
            SELECT new com.akigen.ecotrekk_backend.dto.response.GearAdminListItemDTO(
                hi.id,
                g.id,
                g.name,
                c.name,
                                COALESCE(MAX(gi.imageUrl), c.imageUrl),
                g.description,
                g.dailyRentPrice,
                g.depositAmount,
                hi.totalQty,
                hi.availableQty,
                CASE WHEN g.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END,
                g.updatedAt
            )
                  FROM Inventory hi
            JOIN hi.gear g
            JOIN g.category c
                        LEFT JOIN GearImage gi ON gi.gear = g AND gi.primary = true
            WHERE (:search IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:category IS NULL OR LOWER(c.name) = LOWER(:category))
              AND (:status IS NULL OR (:status = 'ACTIVE' AND g.active = true) OR (:status = 'INACTIVE' AND g.active = false))
                        GROUP BY hi.id, g.id, g.name, c.name, c.imageUrl, g.description, g.dailyRentPrice, g.depositAmount, hi.totalQty, hi.availableQty, g.active, g.updatedAt
            ORDER BY g.updatedAt DESC
            """)
                Page<GearAdminListItemDTO> searchAdminInventory(
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") String status,
            Pageable pageable
    );
}
