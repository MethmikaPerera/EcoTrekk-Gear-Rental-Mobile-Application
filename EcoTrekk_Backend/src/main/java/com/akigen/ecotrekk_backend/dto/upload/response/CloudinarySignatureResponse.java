package com.akigen.ecotrekk_backend.dto.upload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinarySignatureResponse {

    private String cloudName;
    private String apiKey;
    private long timestamp;
    private String folder;
    private String signature;
}
