package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.CartItemRequest;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.model.*;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductService productService;
    @Mock UserService userService;

    @InjectMocks CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");

        cart = new Cart(user);
        cart.setId(1L);

        Category category = new Category("Fruits");
        category.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setName("Apple");
        product.setPrice(new BigDecimal("2.99"));
        product.setQuantity(20);
        product.setCategory(category);
    }

    @Test
    void addItem_createsNewCartItem_whenProductNotInCart() {
        when(userService.findEntityByUsername("john")).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productService.findEntityById(1L)).thenReturn(product);
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        cartService.addItem("john", request);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_throwsBadRequest_whenInsufficientStock() {
        when(userService.findEntityByUsername("john")).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productService.findEntityById(1L)).thenReturn(product);

        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(99); // product only has 20

        assertThatThrownBy(() -> cartService.addItem("john", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void clearCart_removesAllItems() {
        when(userService.findEntityByUsername("john")).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart("john");

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }
}
