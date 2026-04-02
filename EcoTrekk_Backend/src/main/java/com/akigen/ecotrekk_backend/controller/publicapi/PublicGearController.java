package com.akigen.ecotrekk_backend.controller.publicapi;

import com.akigen.ecotrekk_backend.dto.response.GearCategoryOptionDTO;
import com.akigen.ecotrekk_backend.dto.response.PublicFeaturedGearDTO;
import com.akigen.ecotrekk_backend.service.GearService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/gears")
@RequiredArgsConstructor
public class PublicGearController {

    private final GearService gearService;

    @GetMapping("/categories")
    public ResponseEntity<List<GearCategoryOptionDTO>> getGearCategories() {
        return ResponseEntity.ok(gearService.getGearCategories());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<PublicFeaturedGearDTO>> getFeaturedGears(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(gearService.getPublicFeaturedGears(limit));
    }

    @GetMapping
    public ResponseEntity<List<PublicFeaturedGearDTO>> getPublicGears() {
        return ResponseEntity.ok(gearService.getPublicGears());
    }
}
