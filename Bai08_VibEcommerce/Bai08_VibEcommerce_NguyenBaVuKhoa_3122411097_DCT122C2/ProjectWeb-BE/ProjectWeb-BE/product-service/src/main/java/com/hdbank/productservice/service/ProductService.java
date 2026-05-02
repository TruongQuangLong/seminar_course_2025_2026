package com.hdbank.productservice.service;

import com.hdbank.productservice.dto.ProductRequest;
import com.hdbank.productservice.dto.ProductResponse;
import com.hdbank.productservice.model.Product;
import com.hdbank.productservice.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @CircuitBreaker(name = "productService", fallbackMethod = "createProductFallback")
    public void createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product saved successfully: {}", product.getId());
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getAllProductsFallback")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .build())
                .toList();
    }

    // --- Fallback methods ---

    public void createProductFallback(ProductRequest request, Throwable t) {
        log.error("Fallback - Cannot create product: {}", t.getMessage());
        throw new RuntimeException("Product service is temporarily unavailable. Please try again later.");
    }

    public List<ProductResponse> getAllProductsFallback(Throwable t) {
        log.error("Fallback - Cannot fetch products: {}", t.getMessage());
        return Collections.emptyList();
    }
}
