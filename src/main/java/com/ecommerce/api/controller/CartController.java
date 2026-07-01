package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.CartItemRequest;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * GET /api/cart
     * Returns current user's cart with items and total. Auth required.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    /**
     * POST /api/cart/items
     * Adds a product to cart. If already present, increments quantity.
     * Body: { "productId": 1, "quantity": 2 }
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userDetails.getUsername(), request));
    }

    /**
     * PUT /api/cart/items/{itemId}
     * Updates the quantity of a specific cart item.
     * Body: { "productId": 1, "quantity": 5 }
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userDetails.getUsername(), itemId, request));
    }

    /**
     * DELETE /api/cart/items/{itemId}
     * Removes a single item from the cart.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        cartService.removeItem(userDetails.getUsername(), itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/cart
     * Clears all items from the cart.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
