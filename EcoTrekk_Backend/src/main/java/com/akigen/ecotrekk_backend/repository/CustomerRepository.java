package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByFirebaseUid(String firebaseUid);

    Optional<Customer> findByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);
}
