package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HubRepository extends JpaRepository<Hub, Long> {
    List<Hub> findByActiveTrue();
    Optional<Hub> findByName(String name);
}
