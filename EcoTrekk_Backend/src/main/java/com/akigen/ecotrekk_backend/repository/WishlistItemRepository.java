package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByWishlistId(Long wishlistId);

    Optional<WishlistItem> findByWishlistIdAndGearId(Long wishlistId, Long gearId);

    boolean existsByWishlistIdAndGearId(Long wishlistId, Long gearId);

    void deleteByWishlistIdAndGearId(Long wishlistId, Long gearId);
}
