package com.ecommerce.api.dto.response;

import com.ecommerce.api.model.Cart;
import com.ecommerce.api.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;

    public CartResponse(Cart cart) {
        this.cartId = cart.getId();
        this.items = cart.getItems().stream()
                .map(CartItemResponse::new)
                .toList();
        this.totalPrice = this.items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getCartId() { return cartId; }
    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getTotalPrice() { return totalPrice; }

    public static class CartItemResponse {

        private Long cartItemId;
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public CartItemResponse(CartItem item) {
            this.cartItemId = item.getId();
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
            this.productImage = item.getProduct().getImage();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getProduct().getPrice();
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }

        public Long getCartItemId() { return cartItemId; }
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getProductImage() { return productImage; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}
