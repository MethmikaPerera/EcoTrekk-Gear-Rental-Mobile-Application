package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.response.WishlistItemDTO;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.entity.GearImage;
import com.akigen.ecotrekk_backend.entity.Wishlist;
import com.akigen.ecotrekk_backend.entity.WishlistItem;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.repository.GearImageRepository;
import com.akigen.ecotrekk_backend.repository.GearRepository;
import com.akigen.ecotrekk_backend.repository.WishlistItemRepository;
import com.akigen.ecotrekk_backend.repository.WishlistRepository;
import com.akigen.ecotrekk_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CustomerRepository customerRepository;
    private final GearRepository gearRepository;
    private final GearImageRepository gearImageRepository;

    @Override
    public List<WishlistItemDTO> getCustomerWishlist(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));

        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
                .orElse(null);

        if (wishlist == null) {
            return List.of();
        }

        List<WishlistItem> items = wishlistItemRepository.findByWishlistId(wishlist.getId());
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WishlistItemDTO addToWishlist(Long customerId, Long gearId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));

        Gear gear = gearRepository.findById(gearId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Gear not found"));

        // Get or create wishlist
        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .customer(customer)
                            .build();
                    return wishlistRepository.save(newWishlist);
                });

        // Check if item already exists
        if (wishlistItemRepository.existsByWishlistIdAndGearId(wishlist.getId(), gearId)) {
            // Item already in wishlist, just return it
            WishlistItem existingItem = wishlistItemRepository.findByWishlistIdAndGearId(wishlist.getId(), gearId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Wishlist item not found"));
            return convertToDTO(existingItem);
        }

        // Add new item to wishlist
        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlist(wishlist)
                .gear(gear)
                .build();

        WishlistItem savedItem = wishlistItemRepository.save(wishlistItem);
        return convertToDTO(savedItem);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long customerId, Long gearId) {
        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Wishlist not found for customer"));

        wishlistItemRepository.deleteByWishlistIdAndGearId(wishlist.getId(), gearId);
    }

    @Override
    public boolean isInWishlist(Long customerId, Long gearId) {
        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
                .orElse(null);

        if (wishlist == null) {
            return false;
        }

        return wishlistItemRepository.existsByWishlistIdAndGearId(wishlist.getId(), gearId);
    }

    @Override
    public int getWishlistCount(Long customerId) {
        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
                .orElse(null);

        if (wishlist == null) {
            return 0;
        }

        return wishlistItemRepository.findByWishlistId(wishlist.getId()).size();
    }

    @Override
    @Transactional
    public void clearWishlist(Long customerId) {
        Wishlist wishlist = wishlistRepository.findByCustomerId(customerId)
                .orElse(null);

        if (wishlist != null) {
            List<WishlistItem> items = wishlistItemRepository.findByWishlistId(wishlist.getId());
            wishlistItemRepository.deleteAll(items);
        }
    }

    /**
     * Convert WishlistItem entity to DTO
     */
    private WishlistItemDTO convertToDTO(WishlistItem item) {
        Gear gear = item.getGear();
        String imageUrl = resolveGearImageUrl(gear);

        return WishlistItemDTO.builder()
                .id(item.getId())
                .gearId(gear.getId())
                .gearName(gear.getName())
                .category(gear.getCategory() != null ? gear.getCategory().getName() : "Other")
                .pricePerDay(gear.getDailyRentPrice().doubleValue())
                .depositAmount(gear.getDepositAmount().doubleValue())
                .description(gear.getDescription())
                .imageUrl(imageUrl)
                .addedAt(item.getCreatedAt().toEpochMilli())
                .build();
    }

    /**
     * Resolve gear image URL with fallback chain: primary image -> first sorted image -> category image
     */
    private String resolveGearImageUrl(Gear gear) {
        if (gear == null) {
            return null;
        }

        // Try to get primary image
        List<GearImage> primaryImages = gearImageRepository.findByGearIdAndPrimaryTrue(gear.getId());
        if (!primaryImages.isEmpty() && primaryImages.get(0).getImageUrl() != null) {
            return primaryImages.get(0).getImageUrl();
        }

        // Try to get first sorted image
        List<GearImage> allImages = gearImageRepository.findByGearIdOrderBySortOrderAsc(gear.getId());
        if (!allImages.isEmpty() && allImages.get(0).getImageUrl() != null) {
            return allImages.get(0).getImageUrl();
        }

        // Fallback to category image
        if (gear.getCategory() != null && gear.getCategory().getImageUrl() != null) {
            return gear.getCategory().getImageUrl();
        }

        return null;
    }
}
