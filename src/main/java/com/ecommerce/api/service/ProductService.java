package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.ProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.Category;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    public ProductService(ProductRepository productRepository, 
                         CategoryService categoryService,
                         ReviewService reviewService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    @Transactional(readOnly = true)
    @Cacheable("products")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::enrichProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return enrichProductResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchByKeyword(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable)
                .map(this::enrichProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::enrichProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filterByPriceRange(Long categoryId, 
                                                     BigDecimal minPrice, 
                                                     BigDecimal maxPrice,
                                                     Pageable pageable) {
        return productRepository.findByCategoryAndPriceRange(categoryId, minPrice, maxPrice, pageable)
                .map(this::enrichProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getLowStockProducts(Pageable pageable) {
        return productRepository.findLowStockProducts(pageable)
                .map(this::enrichProductResponse);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.getCategoryById(request.getCategoryId());
        Product product = new Product();
        applyRequest(product, request, category);
        return new ProductResponse(productRepository.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findEntityById(id);
        Category category = categoryService.getCategoryById(request.getCategoryId());
        applyRequest(product, request, category);
        return new ProductResponse(productRepository.save(product));
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Call this whenever a product's stock is mutated outside this service
     * (e.g. checkout deducting stock, order cancellation restocking) so the
     * cached product listings don't serve stale quantities.
     */
    @CacheEvict(value = "products", allEntries = true)
    public void evictProductCache() {
        // No-op body — the annotation does the work.
    }

    private ProductResponse enrichProductResponse(Product product) {
        ProductResponse response = new ProductResponse(product);
        response.setAverageRating(reviewService.getAverageRating(product.getId()));
        response.setReviewCount(reviewService.getReviewCount(product.getId()));
        return response;
    }

    private void applyRequest(Product product, ProductRequest req, Category category) {
        product.setName(req.getName());
        product.setImage(req.getImage());
        product.setCategory(category);
        product.setQuantity(req.getQuantity());
        product.setPrice(req.getPrice());
        product.setWeight(req.getWeight());
        product.setDescription(req.getDescription());
    }
}
