package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.AdminLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerFirebaseLoginRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.CustomerProfileUpdateRequestDTO;
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
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
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
    private final Cloudinary cloudinary;

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

    @Override
    @Transactional
    public CustomerAuthResponseDTO updateCustomerProfile(CustomerProfileUpdateRequestDTO request) {
        FirebaseUserPayload firebaseUserPayload = firebaseTokenVerifier.verifyIdToken(request.getIdToken());

        Customer customer = customerRepository.findByFirebaseUid(firebaseUserPayload.getUid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        String updatedFullName = request.getFullName().trim();
        String updatedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String updatedPhone = StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : "";

        if (!updatedEmail.equalsIgnoreCase(customer.getEmail()) && customerRepository.existsByEmail(updatedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        try {
            UserRecord.UpdateRequest firebaseUpdateRequest = new UserRecord.UpdateRequest(customer.getFirebaseUid())
                    .setDisplayName(updatedFullName)
                    .setEmail(updatedEmail);
            FirebaseAuth.getInstance().updateUser(firebaseUpdateRequest);
        } catch (FirebaseAuthException e) {
            log.error("Failed to update Firebase profile for uid {}: {}", customer.getFirebaseUid(), e.getMessage());
            if ("EMAIL_ALREADY_EXISTS".equalsIgnoreCase(e.getAuthErrorCode().name())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update profile");
        }

        customer.setFullName(updatedFullName);
        customer.setEmail(updatedEmail);
        customer.setPhone(updatedPhone);
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

    @Override
    @Transactional
    public CustomerAuthResponseDTO updateCustomerProfileImage(String idToken, MultipartFile imageFile) {
        if (!StringUtils.hasText(idToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idToken is required");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image is required");
        }

        FirebaseUserPayload firebaseUserPayload = firebaseTokenVerifier.verifyIdToken(idToken);

        Customer customer = customerRepository.findByFirebaseUid(firebaseUserPayload.getUid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        String previousImageUrl = customer.getProfilePictureUrl();
        String secureUrl = uploadCustomerImage(customer.getId(), imageFile);
        customer.setProfilePictureUrl(secureUrl);
        Customer savedCustomer = customerRepository.save(customer);

        // Delete previous Cloudinary image only after new upload + DB save succeed.
        deletePreviousProfileImageIfNeeded(previousImageUrl, secureUrl);

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

    private String uploadCustomerImage(Long customerId, MultipartFile imageFile) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageFile.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ecotrekk/customers/" + customerId,
                            "resource_type", "image",
                            "overwrite", true
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null || !StringUtils.hasText(String.valueOf(secureUrl))) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary upload returned empty URL");
            }
            return String.valueOf(secureUrl);
        } catch (IOException e) {
            log.error("Cloudinary upload failed for customer {}: {}", customerId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload profile image. Please try again.");
        }
    }

    private void deletePreviousProfileImageIfNeeded(String previousImageUrl, String currentImageUrl) {
        if (!StringUtils.hasText(previousImageUrl)) {
            return;
        }
        if (previousImageUrl.equals(currentImageUrl)) {
            return;
        }

        String previousPublicId = extractCloudinaryPublicId(previousImageUrl);
        if (!StringUtils.hasText(previousPublicId) || isDefaultProfileImage(previousPublicId)) {
            return;
        }

        try {
            cloudinary.uploader().destroy(previousPublicId, ObjectUtils.asMap("resource_type", "image"));
            log.info("Deleted previous Cloudinary profile image: {}", previousPublicId);
        } catch (IOException e) {
            // Do not fail the request because profile update already succeeded.
            log.warn("Failed to delete previous Cloudinary profile image {}: {}", previousPublicId, e.getMessage());
        }
    }

    private boolean isDefaultProfileImage(String publicId) {
        return "default_user_t9vrsh".equals(publicId) || publicId.endsWith("/default_user_t9vrsh");
    }

    private String extractCloudinaryPublicId(String secureUrl) {
        try {
            URI uri = new URI(secureUrl);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }

            String[] segments = Arrays.stream(path.split("/"))
                    .filter(StringUtils::hasText)
                    .toArray(String[]::new);

            int uploadIndex = -1;
            for (int i = 0; i < segments.length; i++) {
                if ("upload".equals(segments[i])) {
                    uploadIndex = i;
                    break;
                }
            }
            if (uploadIndex < 0 || uploadIndex + 1 >= segments.length) {
                return null;
            }

            int start = uploadIndex + 1;
            if (segments[start].matches("v\\d+")) {
                start++;
            }
            if (start >= segments.length) {
                return null;
            }

            StringBuilder publicId = new StringBuilder();
            for (int i = start; i < segments.length; i++) {
                if (publicId.length() > 0) {
                    publicId.append('/');
                }
                publicId.append(segments[i]);
            }

            int dotIndex = publicId.lastIndexOf(".");
            if (dotIndex > 0) {
                publicId.setLength(dotIndex);
            }

            return publicId.toString();
        } catch (URISyntaxException e) {
            log.warn("Unable to parse Cloudinary URL for deletion: {}", secureUrl);
            return null;
        }
    }
}
