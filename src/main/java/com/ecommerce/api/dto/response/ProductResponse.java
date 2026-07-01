package com.ecommerce.api.dto.response;

import com.ecommerce.api.model.Product;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private String image;
    private Long categoryId;
    private String categoryName;
    private Integer quantity;
    private BigDecimal price;
    private Integer weight;
    private String description;

    public ProductResponse(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.image = p.getImage();
        this.categoryId = p.getCategory().getId();
        this.categoryName = p.getCategory().getName();
        this.quantity = p.getQuantity();
        this.price = p.getPrice();
        this.weight = p.getWeight();
        this.description = p.getDescription();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public Integer getWeight() { return weight; }
    public String getDescription() { return description; }
}
