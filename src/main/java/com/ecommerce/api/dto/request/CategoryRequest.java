package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be 2–100 characters")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
