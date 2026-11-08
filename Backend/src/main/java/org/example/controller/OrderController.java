package org.example.controller;

import org.example.model.Order;
import org.example.model.Product;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.service.AIParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    AIParsingService aiParsingService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitOrder(@RequestBody Map<String, String> body) {
        String rawInput = body.get("rawInput");
        String customerEmail = body.get("customerEmail");

        AIParsingService.ParsedStockResult parsed = aiParsingService.parseOrderInput(rawInput);

        if (parsed.getProductName() == null || parsed.getQuantity() == null) {
            Order order = new Order(customerEmail, null, null, "PENDING_REVIEW");
            return ResponseEntity.ok(orderRepository.save(order));
        }

        Optional<Product> productOpt = productRepository.findByNameIgnoreCase(parsed.getProductName());

        if (productOpt.isEmpty()) {
            Order order = new Order(customerEmail, null, parsed.getQuantity(), "PENDING_REVIEW");
            return ResponseEntity.ok(orderRepository.save(order));
        }

        Product product = productOpt.get();

        if (product.getQuantity() < parsed.getQuantity()) {
            Order order = new Order(customerEmail, product, parsed.getQuantity(), "OUT_OF_STOCK");
            return ResponseEntity.ok(orderRepository.save(order));
        }

        product.setQuantity(product.getQuantity() - parsed.getQuantity());
        productRepository.save(product);

        Order order = new Order(customerEmail, product, parsed.getQuantity(), "CONFIRMED");
        return ResponseEntity.ok(orderRepository.save(order));
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody String newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Order order = orderOpt.get();
        order.setStatus(newStatus);
        orderRepository.save(order);
        return ResponseEntity.ok(order);
    }
}
