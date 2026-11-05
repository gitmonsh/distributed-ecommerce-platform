
package com.ecommerce.catalog.service;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void shouldReturnAllProducts() {
        Product product = new Product(
                1L, "MacBook Pro", "Apple laptop",
                new BigDecimal("1499.00"), 15
        );

        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("MacBook Pro", products.get(0).getName());
        assertEquals(new BigDecimal("1499.00"),
                products.get(0).getPrice());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldGetProductById() {
        Product product = new Product(
                1L, "MacBook Pro", "Apple laptop",
                new BigDecimal("1499.00"), 15
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Optional<Product> result =
                productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("MacBook Pro", result.get().getName());

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldCreateProduct() {
        Product product = new Product(
                null, "iPhone 16", "Apple smartphone",
                new BigDecimal("799.00"), 20
        );

        Product savedProduct = new Product(
                2L, "iPhone 16", "Apple smartphone",
                new BigDecimal("799.00"), 20
        );

        when(productRepository.save(product))
                .thenReturn(savedProduct);

        Product result = productService.createProduct(product);

        assertEquals(2L, result.getId());
        assertEquals("iPhone 16", result.getName());

        verify(productRepository).save(product);
    }

    @Test
    void shouldUpdateProduct() {
        Product existingProduct = new Product(
                1L, "MacBook Pro", "Apple laptop",
                new BigDecimal("1599.00"), 10
        );

        Product updatedProduct = new Product(
                null, "MacBook Pro", "Updated laptop",
                new BigDecimal("1499.00"), 15
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Product> result =
                productService.updateProduct(1L, updatedProduct);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("1499.00"),
                result.get().getPrice());
        assertEquals(15, result.get().getStock());
        assertEquals(1L, result.get().getId());

        verify(productRepository).save(existingProduct);
    }

    @Test
    void shouldDeleteExistingProduct() {
        when(productRepository.existsById(1L))
                .thenReturn(true);

        boolean deleted = productService.deleteProduct(1L);

        assertTrue(deleted);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteMissingProduct() {
        when(productRepository.existsById(999L))
                .thenReturn(false);

        boolean deleted = productService.deleteProduct(999L);

        assertFalse(deleted);
        verify(productRepository, never()).deleteById(999L);
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Product> result =
                productService.getProductById(999L);

        assertTrue(result.isEmpty());

        verify(productRepository).findById(999L);
    }

    @Test
    void shouldNotUpdateMissingProduct() {
        Product updatedProduct = new Product(
                null,
                "MacBook Pro",
                "Updated laptop",
                new BigDecimal("1499.00"),
                15
        );

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Product> result =
                productService.updateProduct(999L, updatedProduct);

        assertTrue(result.isEmpty());

        verify(productRepository).findById(999L);
        verify(productRepository, never())
                .save(any(Product.class));
    }
}
