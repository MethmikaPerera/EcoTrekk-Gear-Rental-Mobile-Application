package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.config.CloudinaryProperties;
import com.akigen.ecotrekk_backend.dto.upload.request.CloudinarySignatureRequest;
import com.akigen.ecotrekk_backend.dto.upload.response.CloudinarySignatureResponse;
import com.akigen.ecotrekk_backend.service.CloudinarySignedUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class CloudinarySignedUploadServiceImpl implements CloudinarySignedUploadService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public CloudinarySignatureResponse generateUploadSignature(CloudinarySignatureRequest request) {
        validateCloudinaryConfig();

        long timestamp = System.currentTimeMillis() / 1000L;
        String folder = resolveFolder(request.getFolderType(), request.getEntityId());

        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("timestamp", timestamp);
        paramsToSign.put("folder", folder);

        Map<String, Object> signedParams = new HashMap<>(paramsToSign);
        cloudinary.signRequest(signedParams, ObjectUtils.asMap(
            "api_key", properties.getApiKey(),
            "api_secret", properties.getApiSecret()
        ));

        String signature = (String) signedParams.get("signature");

        return CloudinarySignatureResponse.builder()
                .cloudName(properties.getCloudName())
                .apiKey(properties.getApiKey())
                .timestamp(timestamp)
                .folder(folder)
                .signature(signature)
                .build();
    }

    private void validateCloudinaryConfig() {
        if (isInvalid(properties.getCloudName()) || isInvalid(properties.getApiKey()) || isInvalid(properties.getApiSecret())) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Cloudinary is not configured on backend. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET.");
        }
    }

    private boolean isInvalid(String value) {
        if (!StringUtils.hasText(value)) {
            return true;
        }
        return value.contains("${") || value.contains("}");
    }

    private String resolveFolder(String folderType, Long entityId) {
        String normalized = folderType == null ? "" : folderType.trim().toLowerCase();

        return switch (normalized) {
            case "gears" -> entityId == null ? "ecotrekk/gears" : "ecotrekk/gears/" + entityId;
            case "hubs" -> entityId == null ? "ecotrekk/hubs" : "ecotrekk/hubs/" + entityId;
            case "customers" -> entityId == null ? "ecotrekk/customers" : "ecotrekk/customers/" + entityId;
            case "categories" -> entityId == null ? "ecotrekk/categories" : "ecotrekk/categories/" + entityId;
            default -> throw new ResponseStatusException(BAD_REQUEST, "Invalid folderType: " + folderType);
        };
    }
}
