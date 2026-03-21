package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.AdminRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.AdminUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.ChangePasswordRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminCustomerPageResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminCustomerSummaryDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminRegisterResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminUserPageResponseDTO;
import com.akigen.ecotrekk_backend.entity.AdminUser;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.entity.Hub;
import com.akigen.ecotrekk_backend.entity.HubManager;
import com.akigen.ecotrekk_backend.enums.AdminRole;
import com.akigen.ecotrekk_backend.repository.AdminUserRepository;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.repository.HubManagerRepository;
import com.akigen.ecotrekk_backend.repository.HubRepository;
import com.akigen.ecotrekk_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final HubRepository hubRepository;
    private final HubManagerRepository hubManagerRepository;
    private final CustomerRepository customerRepository;

    @Override
    public AdminRegisterResponseDTO createAdmin(AdminRegisterRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = request.getUsername().trim();

        if (adminUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (adminUserRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        
        Hub selectedHub = null;
        if (request.getRole() == AdminRole.ADMIN || request.getRole() == AdminRole.STAFF) {
            if (request.getHubId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hub is required when registering an ADMIN or STAFF member");
            }
            selectedHub = hubRepository.findById(request.getHubId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hub not found with ID: " + request.getHubId()));
        }

        AdminUser newAdmin = AdminUser.builder()
                .username(username)
                .email(email)
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();

        AdminUser savedAdmin = adminUserRepository.save(newAdmin);
        log.info("New admin user created: {} with role: {}", savedAdmin.getUsername(), savedAdmin.getRole());
        
        if (selectedHub != null) {
            HubManager hubManager = HubManager.builder()
                    .hub(selectedHub)
                    .adminUser(savedAdmin)
                    .primary(true)
                    .build();
            hubManagerRepository.save(hubManager);
            log.info("Assigned user {} to hub {}", savedAdmin.getUsername(), selectedHub.getName());
        }

        return mapToResponseDTO(savedAdmin);
    }

    @Override
    public AdminUserPageResponseDTO getAdmins(String search, AdminRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // Use empty string if search is null to match everything when using LIKE
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<AdminUser> adminPage = adminUserRepository.searchAdmins(searchTerm, role, pageable);

        return AdminUserPageResponseDTO.builder()
                .content(adminPage.getContent().stream().map(this::mapToResponseDTO).toList())
                .totalElements(adminPage.getTotalElements())
                .totalPages(adminPage.getTotalPages())
                .size(adminPage.getSize())
                .number(adminPage.getNumber())
                .build();
    }

    @Override
    public AdminRegisterResponseDTO updateAdmin(Long adminId, com.akigen.ecotrekk_backend.dto.request.AdminUpdateRequestDTO request) {
        AdminUser existingAdmin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found with ID: " + adminId));

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = request.getUsername().trim();

        if (!existingAdmin.getUsername().equals(username) && adminUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (!existingAdmin.getEmail().equalsIgnoreCase(email) && adminUserRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        // Validate super admin protection, cannot change role or hub of primary super admin
        if (existingAdmin.getRole() == AdminRole.SUPER_ADMIN && existingAdmin.getId().equals(1L)) {
            if (request.getRole() != AdminRole.SUPER_ADMIN) {
                 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot alter the role of the primary super admin");
            }
        }

        // Handle Hub Associations
        if (request.getRole() == AdminRole.ADMIN || request.getRole() == AdminRole.STAFF) {
            if (request.getHubId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hub is required for ADMIN and STAFF roles");
            }
            Hub selectedHub = hubRepository.findById(request.getHubId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hub not found with ID: " + request.getHubId()));

            // Find existing hub manager record, or create if one does not exist
            HubManager existingHubManager = hubManagerRepository.findByAdminUserId(existingAdmin.getId()).stream().findFirst().orElse(null);
            
            if (existingHubManager != null) {
                if (!existingHubManager.getHub().getId().equals(selectedHub.getId())) {
                     existingHubManager.setHub(selectedHub);
                     hubManagerRepository.save(existingHubManager);
                     log.info("Updated hub assignment for admin user {} to {}", existingAdmin.getUsername(), selectedHub.getName());
                }
            } else {
                 HubManager newHubManager = HubManager.builder()
                    .hub(selectedHub)
                    .adminUser(existingAdmin)
                    .primary(true)
                    .build();
                 hubManagerRepository.save(newHubManager);
                 log.info("Created new hub assignment for admin user {} to {}", existingAdmin.getUsername(), selectedHub.getName());
            }

        } else if (request.getRole() == AdminRole.SUPER_ADMIN) {
             // Super Admins shouldn't be tied to a specific local hub
             hubManagerRepository.findByAdminUserId(existingAdmin.getId()).forEach(hubManagerRepository::delete);
        }

        // Update core properties
        existingAdmin.setUsername(username);
        existingAdmin.setEmail(email);
        existingAdmin.setFullName(request.getFullName().trim());
        existingAdmin.setRole(request.getRole());

        AdminUser updatedAdmin = adminUserRepository.save(existingAdmin);
        log.info("Updated admin user properties for {}", updatedAdmin.getUsername());

        return mapToResponseDTO(updatedAdmin);
    }

    @Override
    public void updateAdminStatus(Long adminId, boolean active) {
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));

        if (adminUser.getRole() == AdminRole.SUPER_ADMIN && adminUser.getId().equals(1L)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot deactivate the primary super admin");
        }

        adminUser.setActive(active);
        adminUserRepository.save(adminUser);
        log.info("Admin user {} status updated to {}", adminId, active ? "ACTIVE" : "INACTIVE");
    }

    @Override
    public AdminRegisterResponseDTO getProfile(String userIdString) {
        Long id = Long.parseLong(userIdString);
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponseDTO(adminUser);
    }

    @Override
    public void changePassword(String userIdString, ChangePasswordRequestDTO request) {
        Long id = Long.parseLong(userIdString);
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), adminUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password does not match");
        }

        adminUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(adminUser);
        log.info("Password changed successfully for user id: {}", id);
    }

    @Override
    public AdminCustomerPageResponseDTO getCustomers(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Boolean activeFilter = parseCustomerStatus(status);

        Page<Customer> customerPage = customerRepository.searchCustomers(searchTerm, activeFilter, pageable);

        return AdminCustomerPageResponseDTO.builder()
                .content(customerPage.getContent().stream().map(this::mapCustomerToSummaryDTO).toList())
                .totalElements(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .size(customerPage.getSize())
                .number(customerPage.getNumber())
                .build();
    }

    @Override
    public void updateCustomerStatus(Long customerId, boolean active) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setActive(active);
        customerRepository.save(customer);
        log.info("Customer {} status updated to {}", customerId, active ? "ACTIVE" : "INACTIVE");
    }

    private AdminRegisterResponseDTO mapToResponseDTO(AdminUser admin) {
        Long hubId = null;
        if (admin.getRole() == AdminRole.ADMIN || admin.getRole() == AdminRole.STAFF) {
            HubManager hm = hubManagerRepository.findByAdminUserId(admin.getId()).stream().findFirst().orElse(null);
            if (hm != null) {
                hubId = hm.getHub().getId();
            }
        }

        return AdminRegisterResponseDTO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .fullName(admin.getFullName())
                .role(admin.getRole())
                .active(admin.isActive())
                .createdAt(admin.getCreatedAt())
                .hubId(hubId)
                .build();
    }

    private AdminCustomerSummaryDTO mapCustomerToSummaryDTO(Customer customer) {
        return AdminCustomerSummaryDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .profilePictureUrl(customer.getProfilePictureUrl())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private Boolean parseCustomerStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Use ACTIVE or INACTIVE");
        };
    }
}
