
package com.ecommerce.catalog;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    private ProductService productService;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        productController = new ProductController(productService);
    }

    private Product createSampleProduct() {
        return new Product(
                1L,
                "MacBook Pro",
                "Apple laptop",
                new BigDecimal("1499.00"),
                15
        );
    }

    @Test
    void shouldReturnAllProducts() {
        Product product = createSampleProduct();

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        List<Product> result =
                productController.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("MacBook Pro", result.get(0).getName());

        verify(productService).getAllProducts();
    }

    @Test
    void shouldReturnProductById() {
        Product product = createSampleProduct();

        when(productService.getProductById(1L))
                .thenReturn(Optional.of(product));

        ResponseEntity<Product> response =
                productController.getProductById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MacBook Pro", response.getBody().getName());
    }

    @Test
    void shouldReturn404WhenProductNotFound() {
        when(productService.getProductById(999L))
                .thenReturn(Optional.empty());

        ResponseEntity<Product> response =
                productController.getProductById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldCreateProduct() {
        Product product = createSampleProduct();

        when(productService.createProduct(product))
                .thenReturn(product);

        ResponseEntity<Product> response =
                productController.createProduct(product);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(productService).createProduct(product);
    }

    @Test
    void shouldUpdateExistingProduct() {
        Product product = createSampleProduct();

        when(productService.updateProduct(1L, product))
                .thenReturn(Optional.of(product));

        ResponseEntity<Product> response =
                productController.updateProduct(1L, product);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MacBook Pro", response.getBody().getName());
    }

    @Test
    void shouldReturn404WhenUpdatingMissingProduct() {
        Product product = createSampleProduct();

        when(productService.updateProduct(999L, product))
                .thenReturn(Optional.empty());

        ResponseEntity<Product> response =
                productController.updateProduct(999L, product);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldDeleteExistingProduct() {
        when(productService.deleteProduct(1L))
                .thenReturn(true);

        ResponseEntity<Void> response =
                productController.deleteProduct(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void shouldReturn404WhenDeletingMissingProduct() {
        when(productService.deleteProduct(999L))
                .thenReturn(false);

        ResponseEntity<Void> response =
                productController.deleteProduct(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(productService).deleteProduct(999L);
    }
}
