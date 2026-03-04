package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
