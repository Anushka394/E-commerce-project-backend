package com.ecommerce.api.controller;

import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products?page=0&size=10&sort=name,asc
     * Public — paginated product listing
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    /**
     * GET /api/products/{id}
     * Public — single product detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * GET /api/products/search?keyword=apple
     * Public — search products by keyword (case-insensitive)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByKeyword(keyword, pageable));
    }

    /**
     * GET /api/products/category/{categoryId}
     * Public — filter products by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getByCategory(categoryId, pageable));
    }

    /**
     * GET /api/products/filter?category=1&minPrice=10&maxPrice=100
     * Public — filter by price range
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponse>> filterByPrice(
            @RequestParam Long category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.filterByPriceRange(category, minPrice, maxPrice, pageable));
    }

    /**
     * GET /api/products/stock/low
     * Admin — get low stock products
     */
    @GetMapping("/stock/low")
    public ResponseEntity<Page<ProductResponse>> getLowStockProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getLowStockProducts(pageable));
    }
}
