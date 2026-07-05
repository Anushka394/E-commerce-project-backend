package com.ecommerce.api.integration;

import com.ecommerce.api.model.*;
import com.ecommerce.api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setRole(User.Role.ROLE_USER);
        testUser = userRepository.save(testUser);

        // Create test category
        Category category = new Category("Electronics");
        category = categoryRepository.save(category);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setQuantity(10);
        testProduct.setCategory(category);
        testProduct = productRepository.save(testProduct);

        // Create test cart
        testCart = new Cart(testUser);
        testCart = cartRepository.save(testCart);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testCheckout() throws Exception {
        // Add item to cart
        CartItem cartItem = new CartItem(testCart, testProduct, 2);
        cartItemRepository.save(cartItem);

        var checkoutRequest = new java.util.HashMap<String, String>();
        checkoutRequest.put("shippingAddress", "123 Main St, City");
        checkoutRequest.put("paymentMethod", "stripe");

        // Checkout now attempts payment synchronously via the mock gateway (createOrder ->
        // processPayment), so the resulting status is PROCESSING (success) or CANCELLED
        // (the gateway's simulated ~5% decline rate) — never still PENDING.
        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(anyOf(is("PROCESSING"), is("CANCELLED"))))
                .andExpect(jsonPath("$.totalPrice").value(199.98))
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St, City"))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetUserOrders() throws Exception {
        // Create an order
        Order order = new Order(testUser, new BigDecimal("199.98"), "123 Main St");
        order.setPaymentMethod("stripe");
        orderRepository.save(order);

        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testCheckoutWithEmptyCart() throws Exception {
        var checkoutRequest = new java.util.HashMap<String, String>();
        checkoutRequest.put("shippingAddress", "123 Main St");
        checkoutRequest.put("paymentMethod", "stripe");

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cart is empty. Cannot create order."));
    }
}
