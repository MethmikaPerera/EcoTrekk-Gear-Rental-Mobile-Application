package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.CartAddRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.CartItemDTO;
import com.akigen.ecotrekk_backend.entity.Cart;
import com.akigen.ecotrekk_backend.entity.CartItem;
import com.akigen.ecotrekk_backend.entity.Customer;
import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.entity.GearImage;
import com.akigen.ecotrekk_backend.entity.Hub;
import com.akigen.ecotrekk_backend.entity.Inventory;
import com.akigen.ecotrekk_backend.repository.CartItemRepository;
import com.akigen.ecotrekk_backend.repository.CartRepository;
import com.akigen.ecotrekk_backend.repository.CustomerRepository;
import com.akigen.ecotrekk_backend.repository.GearImageRepository;
import com.akigen.ecotrekk_backend.repository.GearRepository;
import com.akigen.ecotrekk_backend.repository.HubRepository;
import com.akigen.ecotrekk_backend.repository.InventoryRepository;
import com.akigen.ecotrekk_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final GearRepository gearRepository;
    private final GearImageRepository gearImageRepository;
    private final HubRepository hubRepository;
    private final InventoryRepository inventoryRepository;

    @Override
        @Transactional(readOnly = true)
        public List<CartItemDTO> getCartItems(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart not found"));

        return cartItemRepository.findByCartId(cart.getId()).stream()
            .map(this::toDto)
            .toList();
        }

        @Override
    @Transactional
    public CartItemDTO addToCart(Long customerId, CartAddRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));

        Gear gear = gearRepository.findById(request.getGearId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Gear not found"));

        if (request.getQuantity() < 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity must be at least 1");
        }

        // Validate against available inventory
        int availableQty = inventoryRepository.findByGearId(request.getGearId()).stream()
                .mapToInt(Inventory::getAvailableQty)
                .sum();
        if (availableQty <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "This item is currently out of stock");
        }
        if (request.getQuantity() > availableQty) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Only " + availableQty + " unit(s) available for this item");
        }

        LocalDate startDate = parseDate(request.getStartDate(), "startDate");
        LocalDate endDate = parseDate(request.getEndDate(), "endDate");
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "End date must be on or after start date");
        }

        Hub hub = resolveHub(request.getHubId());

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .customer(customer)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build()));

        cart.setStartDate(startDate);
        cart.setEndDate(endDate);
        final Cart savedCart = cartRepository.save(cart);

        CartItem cartItem = cartItemRepository.findByCartIdAndGearIdAndHubId(savedCart.getId(), gear.getId(), hub.getId())
                .map(existing -> {
                    existing.setQty(existing.getQty() + request.getQuantity());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                .cart(savedCart)
                        .gear(gear)
                        .hub(hub)
                        .qty(request.getQuantity())
                        .build());

        CartItem savedItem = cartItemRepository.save(cartItem);
        return toDto(savedItem);
    }

    @Override
    @Transactional
    public void removeCartItem(Long customerId, Long cartItemId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart not found"));

        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart item not found"));

        cartItemRepository.delete(cartItem);
    }

    private Hub resolveHub(Long hubId) {
        if (hubId != null) {
            return hubRepository.findById(hubId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Hub not found"));
        }

        return hubRepository.findByActiveTrue().stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "No active hub found"));
    }

    private LocalDate parseDate(String raw, String field) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid " + field + " format, expected yyyy-MM-dd");
        }
    }

    private CartItemDTO toDto(CartItem item) {
        Cart cart = item.getCart();
        Gear gear = item.getGear();
        Hub hub = item.getHub();

        int availableQty = inventoryRepository.findByGearId(gear.getId()).stream()
                .mapToInt(Inventory::getAvailableQty)
                .sum();

        return CartItemDTO.builder()
                .id(item.getId())
                .cartId(cart.getId())
                .gearId(gear.getId())
                .gearName(gear.getName())
                .category(gear.getCategory() != null ? gear.getCategory().getName() : "Other")
                .pricePerDay(gear.getDailyRentPrice() != null ? gear.getDailyRentPrice().doubleValue() : 0.0)
                .depositAmount(gear.getDepositAmount() != null ? gear.getDepositAmount().doubleValue() : 0.0)
                .description(gear.getDescription())
                .imageUrl(resolveGearImageUrl(gear))
                .hubId(hub.getId())
                .hubName(hub.getName())
                .quantity(item.getQty())
                .startDate(cart.getStartDate() != null ? cart.getStartDate().toString() : null)
                .endDate(cart.getEndDate() != null ? cart.getEndDate().toString() : null)
                .addedAt(item.getCreatedAt() != null ? item.getCreatedAt().toEpochMilli() : null)
                .availableQty(availableQty)
                .build();
    }

    private String resolveGearImageUrl(Gear gear) {
        List<GearImage> primaryImages = gearImageRepository.findByGearIdAndPrimaryTrue(gear.getId());
        if (!primaryImages.isEmpty()) {
            return primaryImages.get(0).getImageUrl();
        }

        List<GearImage> orderedImages = gearImageRepository.findByGearIdOrderBySortOrderAsc(gear.getId());
        if (!orderedImages.isEmpty()) {
            return orderedImages.get(0).getImageUrl();
        }

        return gear.getCategory() != null ? gear.getCategory().getImageUrl() : null;
    }
}
