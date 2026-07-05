package com.ecommerce.api.service;

import com.ecommerce.api.model.Order;
import com.ecommerce.api.model.Payment;
import com.ecommerce.api.repository.PaymentRepository;
import com.ecommerce.api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public Payment createPayment(Order order) {
        Payment payment = new Payment(order, order.getTotalPrice());
        payment.setPaymentGateway(order.getPaymentMethod());
        return paymentRepository.save(payment);
    }

    public Payment processPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Mock payment processing
        try {
            log.info("Processing payment for Order: {}, Amount: {}, Gateway: {}", 
                    orderId, order.getTotalPrice(), order.getPaymentMethod());

            // Simulate payment gateway call
            String transactionId = "txn_" + UUID.randomUUID().toString().substring(0, 12);
            
            // In real implementation, call Stripe/Razorpay API here
            // For now, assume 95% success rate
            boolean isSuccess = Math.random() > 0.05;

            if (isSuccess) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setTransactionId(transactionId);
                order.setTransactionId(transactionId);
                order.setStatus(Order.OrderStatus.PROCESSING);
                log.info("Payment successful. TransactionId: {}", transactionId);
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setErrorMessage("Payment gateway declined");
                order.setStatus(Order.OrderStatus.CANCELLED);
                log.warn("Payment failed for Order: {}", orderId);
            }

            paymentRepository.save(payment);
            orderRepository.save(order);

            return payment;
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            paymentRepository.save(payment);
            log.error("Payment processing error: {}", e.getMessage());
            throw e;
        }
    }

    public void refundPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("Payment refunded for Order: {}, TransactionId: {}", orderId, payment.getTransactionId());
        }
    }
}
