package org.example.controller;

import org.example.model.Order;
import org.example.model.Product;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.service.AIParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);

        controller = new OrderController();
        controller.orderRepository = orderRepository;
        controller.productRepository = productRepository;
        controller.aiParsingService = new AIParsingService(); // real instance
    }

    @Test
    void submit_confirmsOrder_andDeductsStock_whenEnoughAvailable() {
        Map<String, String> body = new HashMap<>();
        body.put("rawInput", "5 Rice 10kg");
        body.put("customerEmail", "customer@stackfive.com");

        Product rice = new Product("Rice 10kg", 20, 5);

        when(productRepository.findByNameIgnoreCase("Rice 10kg")).thenReturn(Optional.of(rice));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.submitOrder(body);
        Order saved = (Order) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("CONFIRMED", saved.getStatus());
        assertEquals(15, rice.getQuantity());
        verify(productRepository, times(1)).save(rice);
    }

    @Test
    void submit_marksOutOfStock_whenNotEnoughAvailable_andDoesNotDeduct() {
        Map<String, String> body = new HashMap<>();
        body.put("rawInput", "500 Rice 10kg");
        body.put("customerEmail", "customer@stackfive.com");

        Product rice = new Product("Rice 10kg", 20, 5);

        when(productRepository.findByNameIgnoreCase("Rice 10kg")).thenReturn(Optional.of(rice));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.submitOrder(body);
        Order saved = (Order) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("OUT_OF_STOCK", saved.getStatus());
        assertEquals(20, rice.getQuantity()); // unchanged
        verify(productRepository, never()).save(any());
    }

    @Test
    void submit_marksPendingReview_whenProductNotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("rawInput", "3 Flour 2kg");
        body.put("customerEmail", "customer@stackfive.com");

        when(productRepository.findByNameIgnoreCase("Flour 2kg")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.submitOrder(body);
        Order saved = (Order) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("PENDING_REVIEW", saved.getStatus());
        verify(productRepository, never()).save(any());
    }

    @Test
    void submit_marksPendingReview_whenInputCannotBeParsed() {
        Map<String, String> body = new HashMap<>();
        body.put("rawInput", "hello there");
        body.put("customerEmail", "customer@stackfive.com");

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.submitOrder(body);
        Order saved = (Order) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("PENDING_REVIEW", saved.getStatus());
    }
}
