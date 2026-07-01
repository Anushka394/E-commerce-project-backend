package com.ecommerce.api.controller;

import com.ecommerce.api.dto.request.CategoryRequest;
import com.ecommerce.api.dto.request.ProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.model.Category;
import com.ecommerce.api.service.CategoryService;
import com.ecommerce.api.service.ProductService;
import com.ecommerce.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All routes here require ROLE_ADMIN (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final ProductService productService;

    public AdminController(UserService userService,
                           CategoryService categoryService,
                           ProductService productService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    // ── Customer management ───────────────────────────────────────────────────

    /** GET /api/admin/customers */
    @GetMapping("/customers")
    public ResponseEntity<List<UserResponse>> getAllCustomers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** GET /api/admin/customers/{id} */
    @GetMapping("/customers/{id}")
    public ResponseEntity<UserResponse> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /** DELETE /api/admin/customers/{id} */
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ── Category management ───────────────────────────────────────────────────

    /** POST /api/admin/categories */
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    /** PUT /api/admin/categories/{id} */
    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                    @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    /** DELETE /api/admin/categories/{id} */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ── Product management ────────────────────────────────────────────────────

    /** POST /api/admin/products */
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    /** PUT /api/admin/products/{id} */
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                          @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /** DELETE /api/admin/products/{id} */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
