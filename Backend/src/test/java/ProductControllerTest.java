package org.example.controller;

import org.example.model.Product;
import org.example.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    private ProductRepository productRepository;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productController = new ProductController();
        productController.productRepository = productRepository;
    }

    @Test
    void addProduct_savesAndReturnsProduct() {
        Product product = new Product("Rice 10kg", 15, 5);
        when(productRepository.save(product)).thenReturn(product);

        ResponseEntity<?> response = productController.addProduct(product);

        assertEquals(200, response.getStatusCode().value());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void getLowStockProducts_returnsOnlyItemsAtOrBelowThreshold() {
        Product rice = new Product("Rice 10kg", 10, 5);      // not low
        Product oil = new Product("Cooking Oil 5L", 2, 5);   // low

        when(productRepository.findAll()).thenReturn(Arrays.asList(rice, oil));

        ResponseEntity<?> response = productController.getLowStockProducts();
        List<?> body = (List<?>) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, body.size());
        assertTrue(body.contains(oil));
        assertFalse(body.contains(rice));
    }

    @Test
    void updateQuantity_updatesExistingProduct() {
        Product product = new Product("Rice 10kg", 15, 5);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ResponseEntity<?> response = productController.updateQuantity(1L, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, product.getQuantity());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void updateQuantity_returnsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = productController.updateQuantity(99L, 10);

        assertEquals(404, response.getStatusCode().value());
    }
}