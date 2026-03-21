package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.AdminRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.ChangePasswordRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminCustomerPageResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminRegisterResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminUserPageResponseDTO;
import com.akigen.ecotrekk_backend.enums.AdminRole;

public interface AdminService {
    AdminRegisterResponseDTO createAdmin(AdminRegisterRequestDTO request);
    AdminUserPageResponseDTO getAdmins(String search, AdminRole role, int page, int size);
    AdminRegisterResponseDTO updateAdmin(Long adminId, com.akigen.ecotrekk_backend.dto.request.AdminUpdateRequestDTO request);
    void updateAdminStatus(Long adminId, boolean active);
    AdminRegisterResponseDTO getProfile(String username);
    void changePassword(String username, ChangePasswordRequestDTO request);

    AdminCustomerPageResponseDTO getCustomers(String search, String status, int page, int size);
    void updateCustomerStatus(Long customerId, boolean active);
}
