package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.CartItemRequest;
import com.ecommerce.api.dto.response.CartResponse;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.Cart;
import com.ecommerce.api.model.CartItem;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.model.User;
import com.ecommerce.api.repository.CartItemRepository;
import com.ecommerce.api.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductService productService,
                       UserService userService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String username) {
        Cart cart = getOrCreateCart(username);
        // Re-fetch inside same transaction to ensure items are loaded
        return new CartResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    public CartResponse addItem(String username, CartItemRequest request) {
        Cart cart = getOrCreateCart(username);
        Product product = productService.findEntityById(request.getProductId());

        validateStock(product, request.getQuantity());

        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(
                        existing -> {
                            int newQty = existing.getQuantity() + request.getQuantity();
                            validateStock(product, newQty);
                            existing.setQuantity(newQty);
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            CartItem item = new CartItem(cart, product, request.getQuantity());
                            cart.getItems().add(item);
                            cartItemRepository.save(item);
                        }
                );

        return new CartResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    public CartResponse updateItem(String username, Long cartItemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(username);
        CartItem item = findCartItem(cartItemId, cart.getId());

        validateStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return new CartResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    public void removeItem(String username, Long cartItemId) {
        Cart cart = getOrCreateCart(username);
        CartItem item = findCartItem(cartItemId, cart.getId());
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }

    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cart getOrCreateCart(String username) {
        User user = userService.findEntityByUsername(username);
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private CartItem findCartItem(Long cartItemId, Long cartId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        if (!item.getCart().getId().equals(cartId)) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }
        return item;
    }

    private void validateStock(Product product, int requestedQty) {
        if (product.getQuantity() < requestedQty) {
            throw new BadRequestException(
                    "Insufficient stock for '" + product.getName()
                    + "'. Available: " + product.getQuantity());
        }
    }
}
