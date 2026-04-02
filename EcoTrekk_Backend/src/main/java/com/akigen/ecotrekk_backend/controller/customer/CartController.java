package com.akigen.ecotrekk_backend.controller.customer;

import com.akigen.ecotrekk_backend.dto.request.CartAddRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.CartItemDTO;
import com.akigen.ecotrekk_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItemDTO> addToCart(
            @PathVariable Long customerId,
            @Valid @RequestBody CartAddRequestDTO request) {
        CartItemDTO cartItem = cartService.addToCart(customerId, request);
        return ResponseEntity.ok(cartItem);
    }

    @GetMapping("/{customerId}/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCartItems(customerId));
    }

    @DeleteMapping("/{customerId}/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long customerId,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(customerId, cartItemId);
        return ResponseEntity.noContent().build();
    }
}
