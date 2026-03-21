package com.akigen.ecotrekk_backend.security.jwt;

import com.akigen.ecotrekk_backend.entity.AdminUser;

public interface JwtService {
    String generateAdminAccessToken(AdminUser adminUser);
    long getAccessTokenExpiresInSeconds();
    String extractUsername(String token);
    boolean isTokenValid(String token);
    String extractRole(String token);
}
