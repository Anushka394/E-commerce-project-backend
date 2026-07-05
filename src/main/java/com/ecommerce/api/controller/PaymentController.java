package com.ecommerce.api.controller;

import com.ecommerce.api.model.Payment;
import com.ecommerce.api.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments/{orderId}/process
     * Process payment for an order (mock implementation)
     * Admin or system-only
     */
    @PostMapping("/{orderId}/process")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Payment> processPayment(@PathVariable Long orderId) {
        Payment payment = paymentService.processPayment(orderId);
        return ResponseEntity.ok(payment);
    }
}
