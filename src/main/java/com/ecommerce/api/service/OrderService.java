package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.CheckoutRequest;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.*;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final PaymentService paymentService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                       CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserService userService,
                       PaymentService paymentService,
                       ProductService productService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.paymentService = paymentService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String username, Pageable pageable) {
        User user = userService.findEntityByUsername(username);
        return orderRepository.findByUser(user, pageable).map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, String username) {
        User user = userService.findEntityByUsername(username);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return new OrderResponse(order);
    }

    public OrderResponse createOrder(String username, CheckoutRequest request) {
        User user = userService.findEntityByUsername(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot create order.");
        }

        // Create order from cart
        Order order = new Order(user, BigDecimal.ZERO, request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;

        // Convert cart items to order items with inventory locking.
        // Only unexpected failures get wrapped/logged generically — known business
        // exceptions (like insufficient stock) must propagate with their real type
        // so the client gets an accurate error rather than a generic 400.
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Check inventory
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new com.ecommerce.api.exception.InsufficientStockException(
                        "Insufficient stock for '" + product.getName() + "'. " +
                        "Available: " + product.getQuantity() + ", Requested: " + cartItem.getQuantity()
                );
            }

            // Deduct inventory (optimistic locking will handle concurrent purchase races)
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());

            // Create order item with price snapshot
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    product.getPrice()
            );
            order.getItems().add(orderItem);
            totalPrice = totalPrice.add(orderItem.getSubtotal());
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        // Create the payment record, then attempt to charge it immediately (mock gateway).
        // Order status is updated inside processPayment (PROCESSING on success, CANCELLED on decline).
        paymentService.createPayment(savedOrder);
        Payment payment = paymentService.processPayment(savedOrder.getId());

        if (payment.getStatus() == Payment.PaymentStatus.FAILED) {
            // Payment was declined — release the stock we reserved above so it's sellable again.
            for (OrderItem item : savedOrder.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
            }
            log.warn("Checkout payment declined, stock released. OrderId: {}", savedOrder.getId());
        }

        // Clear cart regardless of payment outcome — the checkout attempt is complete either way.
        cart.getItems().clear();
        cartRepository.save(cart);

        // Stock quantities changed above — invalidate cached product listings.
        productService.evictProductCache();

        log.info("Order created. OrderId: {}, UserId: {}, Total: {}, PaymentStatus: {}",
                savedOrder.getId(), user.getId(), totalPrice, payment.getStatus());

        return new OrderResponse(savedOrder);
    }

    public OrderResponse updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: '" + newStatus +
                    "'. Valid values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED");
        }
        order.setStatus(status);

        if (status == Order.OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        } else if (status == Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order updated = orderRepository.save(order);
        log.info("Order status updated. OrderId: {}, NewStatus: {}", orderId, newStatus);

        return new OrderResponse(updated);
    }

    public void cancelOrder(Long orderId, String username) {
        User user = userService.findEntityByUsername(username);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel order in " + order.getStatus() + " status");
        }

        // Refund inventory
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Refund payment if applicable
        paymentService.refundPayment(orderId);

        // Stock quantities changed above — invalidate cached product listings.
        productService.evictProductCache();

        log.info("Order cancelled. OrderId: {}, UserId: {}", orderId, user.getId());
    }
}
