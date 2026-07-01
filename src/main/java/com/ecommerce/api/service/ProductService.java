package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.ProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.Category;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::new);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return new ProductResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream().map(ProductResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream().map(ProductResponse::new).toList();
    }

    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.getCategoryById(request.getCategoryId());
        Product product = applyRequest(new Product(), request, category);
        return new ProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findEntityById(id);
        Category category = categoryService.getCategoryById(request.getCategoryId());
        applyRequest(product, request, category);
        return new ProductResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Product applyRequest(Product product, ProductRequest req, Category category) {
        product.setName(req.getName());
        product.setImage(req.getImage());
        product.setCategory(category);
        product.setQuantity(req.getQuantity());
        product.setPrice(req.getPrice());
        product.setWeight(req.getWeight());
        product.setDescription(req.getDescription());
        return product;
    }
}
