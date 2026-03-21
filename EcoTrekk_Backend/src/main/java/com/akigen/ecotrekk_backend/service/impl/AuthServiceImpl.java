package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.AdminLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerFirebaseLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerRegisterRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminLoginResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminUserSummaryDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerAuthResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerRegisterResponseDTO;
import com.akigen.ecotrekk_backend.entity.AdminUser;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.repository.AdminUserRepository;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.security.firebase.FirebaseTokenVerifier;
import com.akigen.ecotrekk_backend.security.firebase.FirebaseUserPayload;
import com.akigen.ecotrekk_backend.security.jwt.JwtService;
import com.akigen.ecotrekk_backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AdminUserRepository adminUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    // ── Admin login ───────────────────────────────────────────────────────────

    @Override
    public AdminLoginResponseDTO adminLogin(AdminLoginRequestDTO adminLoginRequest) {
        String email = adminLoginRequest.getEmail().trim();

        AdminUser admin = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!admin.isActive()) {
            throw new DisabledException("Admin account is suspended");
        }

        boolean truePassword = passwordEncoder.matches(adminLoginRequest.getPassword(), admin.getPasswordHash());
        if (!truePassword) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateAdminAccessToken(admin);

        return AdminLoginResponseDTO.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresInSeconds(jwtService.getAccessTokenExpiresInSeconds())
                .adminUser(AdminUserSummaryDTO.builder()
                        .id(admin.getId())
                        .username(admin.getUsername())
                        .fullName(admin.getFullName())
                        .email(admin.getEmail())
                        .role(admin.getRole())
                        .build())
                .build();
    }

    // ── Customer: Firebase ID-token login ─────────────────────────────────────

    @Override
    @Transactional
    public CustomerAuthResponseDTO customerLoginWithFirebase(CustomerFirebaseLoginRequestDTO customerLoginRequest) {
        FirebaseUserPayload firebaseUserPayload = firebaseTokenVerifier
                .verifyIdToken(customerLoginRequest.getIdToken());

        String email = firebaseUserPayload.getEmail() == null ? null
                : firebaseUserPayload.getEmail().trim().toLowerCase(Locale.ROOT);

        Customer customer = customerRepository.findByFirebaseUid(firebaseUserPayload.getUid())
                .orElseGet(Customer::new);

        if (customer.getId() == null) {
            customer.setFirebaseUid(firebaseUserPayload.getUid());
            customer.setActive(true);
        }

        String ensuredFullName = StringUtils.hasText(firebaseUserPayload.getFullName())
                ? firebaseUserPayload.getFullName().trim()
                : "EcoTrekk Explorer";

        String ensuredEmail = StringUtils.hasText(email)
                ? email
                : firebaseUserPayload.getUid().toLowerCase(Locale.ROOT) + "@firebase.local";

        customer.setFullName(ensuredFullName);
        customer.setEmail(ensuredEmail);

        if (StringUtils.hasText(firebaseUserPayload.getPhone())) {
            customer.setPhone(firebaseUserPayload.getPhone().trim());
        }
        if (StringUtils.hasText(firebaseUserPayload.getProfilePictureUrl())) {
            customer.setProfilePictureUrl(firebaseUserPayload.getProfilePictureUrl().trim());
        }

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerAuthResponseDTO.builder()
                .customerId(savedCustomer.getId())
                .firebaseUid(savedCustomer.getFirebaseUid())
                .fullName(savedCustomer.getFullName())
                .email(savedCustomer.getEmail())
                .phone(savedCustomer.getPhone())
                .profilePictureUrl(savedCustomer.getProfilePictureUrl())
                .active(savedCustomer.isActive())
                .build();
    }

    // ── Customer: Backend-driven registration ─────────────────────────────────

    @Override
    @Transactional
    public CustomerRegisterResponseDTO registerCustomer(CustomerRegisterRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        // 1. Check for existing MySQL record to give a friendly error early
        if (customerRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        // 2. Create Firebase Auth user via Admin SDK
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(request.getPassword())
                .setDisplayName(request.getFullName().trim())
                .setEmailVerified(false)
                .setDisabled(false);

        UserRecord firebaseUser;
        try {
            firebaseUser = FirebaseAuth.getInstance().createUser(createRequest);
        } catch (FirebaseAuthException e) {
            log.error("Firebase createUser failed for {}: {}", email, e.getMessage());
            if ("EMAIL_EXISTS".equalsIgnoreCase(e.getAuthErrorCode().name())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not create account. Please try again.");
        }

        // 3. Save customer record to MySQL
        Customer customer = Customer.builder()
                .firebaseUid(firebaseUser.getUid())
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(request.getPhone())
                .profilePictureUrl(
                        "https://res.cloudinary.com/drzwswfsc/image/upload/v1774085003/default_user_t9vrsh.jpg")
                .active(true)
                .build();

        Customer saved = customerRepository.save(customer);

        // 4. Create a Firebase custom token so the app can call signInWithCustomToken()
        String customToken;
        try {
            customToken = FirebaseAuth.getInstance().createCustomToken(firebaseUser.getUid());
        } catch (FirebaseAuthException e) {
            log.error("createCustomToken failed for uid {}: {}", firebaseUser.getUid(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Account created, but token generation failed. Please log in.");
        }

        return CustomerRegisterResponseDTO.builder()
                .customToken(customToken)
                .customerId(saved.getId())
                .firebaseUid(saved.getFirebaseUid())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .profilePictureUrl(saved.getProfilePictureUrl())
                .build();
    }
}
