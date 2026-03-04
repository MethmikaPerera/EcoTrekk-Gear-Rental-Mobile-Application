package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HubRepository extends JpaRepository<Hub, Long> {
    List<Hub> findByActiveTrue();

    List<Hub> findByCityIgnoreCaseAndActiveTrue(String city);
}
