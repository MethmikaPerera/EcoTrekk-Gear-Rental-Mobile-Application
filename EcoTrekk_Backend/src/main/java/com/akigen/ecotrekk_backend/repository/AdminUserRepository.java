package com.akigen.ecotrekk_backend.repository;

import com.akigen.ecotrekk_backend.entity.AdminUser;
import com.akigen.ecotrekk_backend.enums.AdminRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    @Query("SELECT a FROM AdminUser a WHERE " +
           "(:role IS NULL OR a.role = :role) AND " +
           "(:search IS NULL OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AdminUser> searchAdmins(@Param("search") String search, @Param("role") AdminRole role, Pageable pageable);
}
