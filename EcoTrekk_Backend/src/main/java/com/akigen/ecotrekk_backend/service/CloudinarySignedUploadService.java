package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.upload.request.CloudinarySignatureRequest;
import com.akigen.ecotrekk_backend.dto.upload.response.CloudinarySignatureResponse;

public interface CloudinarySignedUploadService {
    CloudinarySignatureResponse generateUploadSignature(CloudinarySignatureRequest request);
}
