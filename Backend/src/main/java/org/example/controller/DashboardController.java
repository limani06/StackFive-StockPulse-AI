package org.example.controller;

import org.example.model.Product;
import org.example.model.StockUpdate;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.repository.StockUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    StockUpdateRepository stockUpdateRepository;

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        List<Product> allProducts = productRepository.findAll();

        List<Product> lowStockProducts = allProducts.stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());

        long pendingReviewCount = stockUpdateRepository.findAll().stream()
                .filter(u -> "UNMATCHED".equals(u.getStatus()))
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", allProducts.size());
        summary.put("lowStockCount", lowStockProducts.size());
        summary.put("lowStockProducts", lowStockProducts);
        summary.put("totalOrders", orderRepository.findAll().size());
        summary.put("pendingReviewCount", pendingReviewCount);

        return ResponseEntity.ok(summary);
    }
}
