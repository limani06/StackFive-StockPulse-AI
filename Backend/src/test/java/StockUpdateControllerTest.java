package org.example.controller;

import org.example.model.Product;
import org.example.model.StockUpdate;
import org.example.repository.ProductRepository;
import org.example.repository.StockUpdateRepository;
import org.example.service.AIParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockUpdateControllerTest {

    private StockUpdateRepository stockUpdateRepository;
    private ProductRepository productRepository;
    private StockUpdateController controller;

    @BeforeEach
    void setUp() {
        stockUpdateRepository = mock(StockUpdateRepository.class);
        productRepository = mock(ProductRepository.class);

        controller = new StockUpdateController();
        controller.stockUpdateRepository = stockUpdateRepository;
        controller.productRepository = productRepository;
        controller.aiParsingService = new AIParsingService(); // real instance, not mocked
    }

    @Test
    void submit_updatesProductQuantity_whenProductMatched() {
        Map<String, String> body = new HashMap<>();
        body.put("rawInput", "Received 10 Rice 10kg");
        body.put("submittedByEmail", "staff@stackfive.com");

        Product rice = new Product("Rice 10kg", 10, 5);

        when(productRepository.findByNameIgnoreCase("Rice 10kg")).thenReturn(Optional.of(rice));
        when(stockUpdateRepository.save(any(StockUpdate.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.submitStockUpdate(body);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(20, rice.getQuantity());
        verify(productRepository, times(1)).save(rice);
    }

    @Test
    void submit_marksUnmatched_whenProductNotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("rawInput", "Received 5 Sugar 2kg");
        body.put("submittedByEmail", "staff@stackfive.com");

        when(productRepository.findByNameIgnoreCase("Sugar 2kg")).thenReturn(Optional.empty());
        when(stockUpdateRepository.save(any(StockUpdate.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.submitStockUpdate(body);
        StockUpdate saved = (StockUpdate) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("UNMATCHED", saved.getStatus());
        verify(productRepository, never()).save(any());
    }
}