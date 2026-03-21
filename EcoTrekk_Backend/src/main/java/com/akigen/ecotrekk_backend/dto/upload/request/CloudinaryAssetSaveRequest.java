package com.akigen.ecotrekk_backend.dto.upload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CloudinaryAssetSaveRequest {

    @NotBlank
    private String secureUrl;

    @NotBlank
    private String publicId;

    @NotBlank
    private String folderType;

    @NotNull
    private Long entityId;

    private Boolean primaryImage;
    private Integer sortOrder;
}
