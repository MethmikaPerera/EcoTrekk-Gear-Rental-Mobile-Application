package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.HubManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HubManagerRepository extends JpaRepository<HubManager, Long> {

    List<HubManager> findByHubId(Long hubId);

    List<HubManager> findByAdminUserId(Long adminUserId);

    Optional<HubManager> findFirstByAdminUserId(Long adminUserId);

    boolean existsByHubIdAndAdminUserId(Long hubId, Long adminUserId);
}
