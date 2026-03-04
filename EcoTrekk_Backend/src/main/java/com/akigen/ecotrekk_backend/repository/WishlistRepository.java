package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
