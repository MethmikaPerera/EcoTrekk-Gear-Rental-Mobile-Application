package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.AdminLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerFirebaseLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerProfileUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminLoginResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerAuthResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerRegisterResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AdminLoginResponseDTO adminLogin(AdminLoginRequestDTO adminLoginRequest);

    CustomerAuthResponseDTO customerLoginWithFirebase(CustomerFirebaseLoginRequestDTO customerLoginRequest);

    CustomerRegisterResponseDTO registerCustomer(CustomerRegisterRequestDTO request);

    CustomerAuthResponseDTO updateCustomerProfile(CustomerProfileUpdateRequestDTO request);

    CustomerAuthResponseDTO updateCustomerProfileImage(String idToken, MultipartFile imageFile);
}
