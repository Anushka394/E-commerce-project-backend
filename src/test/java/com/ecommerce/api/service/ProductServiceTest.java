package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.ProductRequest;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.model.Category;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryService categoryService;

    @InjectMocks ProductService productService;

    @Test
    void createProduct_savesAndReturnsResponse() {
        Category category = new Category("Fruits");
        category.setId(1L);

        ProductRequest request = new ProductRequest();
        request.setName("Apple");
        request.setCategoryId(1L);
        request.setPrice(new BigDecimal("2.99"));
        request.setQuantity(50);
        request.setWeight(100);
        request.setDescription("Fresh apples");

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Apple");
        saved.setCategory(category);
        saved.setPrice(new BigDecimal("2.99"));
        saved.setQuantity(50);
        saved.setWeight(100);
        saved.setDescription("Fresh apples");

        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getName()).isEqualTo("Apple");
        assertThat(response.getPrice()).isEqualByComparingTo("2.99");
        assertThat(response.getCategoryName()).isEqualTo("Fruits");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_throwsNotFound_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteProduct_throwsNotFound_whenMissing() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
