package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.CheckoutRequest;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * GET /api/orders
     * Retrieve authenticated user's orders with pagination
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername(), pageable));
    }

    /**
     * GET /api/orders/{orderId}
     * Get single order by ID (user can only see their own)
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, userDetails.getUsername()));
    }

    /**
     * POST /api/orders/checkout
     * Create order from cart and initiate payment
     * Body: { "shippingAddress": "...", "paymentMethod": "stripe" }
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        OrderResponse order = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * DELETE /api/orders/{orderId}
     * Cancel order (only if pending or processing)
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
