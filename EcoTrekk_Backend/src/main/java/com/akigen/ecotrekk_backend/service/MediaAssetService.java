package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.upload.request.CloudinaryAssetSaveRequest;

public interface MediaAssetService {
    void saveUploadedAsset(CloudinaryAssetSaveRequest request);
}
