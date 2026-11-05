
package com.ecommerce.catalog;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ProductRepository productRepository;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        productRepository.deleteAll();
    }

    private Product createSampleProduct() {
        return new Product(
                null,
                "MacBook Pro",
                "Apple laptop",
                new BigDecimal("1499.00"),
                15
        );
    }

    // TEST 1: Get all products

    @Test
    void shouldGetAllProducts() {
        productRepository.save(createSampleProduct());

        Product[] products = restClient.get()
                .uri("/api/products")
                .retrieve()
                .body(Product[].class);

        assertNotNull(products);
        assertEquals(1, products.length);
        assertEquals("MacBook Pro", products[0].getName());
    }

    // TEST 2: Create a valid product

    @Test
    void shouldCreateProduct() {
        Product product = new Product(
                null,
                "iPhone 16",
                "Apple smartphone",
                new BigDecimal("799.00"),
                20
        );

        ResponseEntity<Product> response =
                restClient.post()
                        .uri("/api/products")
                        .body(product)
                        .retrieve()
                        .toEntity(Product.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(1, productRepository.count());
    }

    // TEST 3: Reject invalid product

    @Test
    void shouldRejectInvalidProduct() {
        Product invalidProduct = new Product(
                null,
                "",
                "Invalid product",
                new BigDecimal("-10.00"),
                -5
        );

        ResponseEntity<String> response =
                restClient.post()
                        .uri("/api/products")
                        .body(invalidProduct)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 400,
                                (request, result) -> {
                                    // Allow asserting the 400 response.
                                }
                        )
                        .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, productRepository.count());
    }

    // TEST 4: Get product by ID

    @Test
    void shouldGetProductById() {
        Product savedProduct =
                productRepository.save(createSampleProduct());

        ResponseEntity<Product> response =
                restClient.get()
                        .uri("/api/products/{id}", savedProduct.getId())
                        .retrieve()
                        .toEntity(Product.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(savedProduct.getId(), response.getBody().getId());
        assertEquals("MacBook Pro", response.getBody().getName());
    }

    // TEST 5: Get nonexistent product

    @Test
    void shouldReturn404ForMissingProduct() {
        ResponseEntity<String> response =
                restClient.get()
                        .uri("/api/products/{id}", 999999L)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
                                (request, result) -> {
                                    // Allow asserting the 404 response.
                                }
                        )
                        .toEntity(String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // TEST 6: Update existing product

    @Test
    void shouldUpdateExistingProduct() {
        Product savedProduct =
                productRepository.save(createSampleProduct());

        Product updatedProduct = new Product(
                null,
                "MacBook Pro M4",
                "Updated Apple laptop",
                new BigDecimal("1799.00"),
                25
        );

        ResponseEntity<Product> response =
                restClient.put()
                        .uri("/api/products/{id}", savedProduct.getId())
                        .body(updatedProduct)
                        .retrieve()
                        .toEntity(Product.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MacBook Pro M4", response.getBody().getName());
        assertEquals(
                new BigDecimal("1799.00"),
                response.getBody().getPrice()
        );
        assertEquals(25, response.getBody().getStock());

        Product databaseProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        assertEquals("MacBook Pro M4", databaseProduct.getName());
        assertEquals(
                new BigDecimal("1799.00"),
                databaseProduct.getPrice()
        );
        assertEquals(25, databaseProduct.getStock());
    }

    // TEST 7: Update nonexistent product

    @Test
    void shouldReturn404WhenUpdatingMissingProduct() {
        Product updatedProduct = createSampleProduct();

        ResponseEntity<String> response =
                restClient.put()
                        .uri("/api/products/{id}", 999999L)
                        .body(updatedProduct)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
                                (request, result) -> {
                                    // Allow asserting the 404 response.
                                }
                        )
                        .toEntity(String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, productRepository.count());
    }

    // TEST 8: Delete existing product

    @Test
    void shouldDeleteExistingProduct() {
        Product savedProduct =
                productRepository.save(createSampleProduct());

        ResponseEntity<Void> response =
                restClient.delete()
                        .uri("/api/products/{id}", savedProduct.getId())
                        .retrieve()
                        .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertFalse(
                productRepository.existsById(savedProduct.getId())
        );

        assertEquals(0, productRepository.count());
    }

    // TEST 9: Delete nonexistent product

    @Test
    void shouldReturn404WhenDeletingMissingProduct() {
        ResponseEntity<Void> response =
                restClient.delete()
                        .uri("/api/products/{id}", 999999L)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
                                (request, result) -> {
                                    // Allow asserting the 404 response.
                                }
                        )
                        .toBodilessEntity();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, productRepository.count());
    }
}
