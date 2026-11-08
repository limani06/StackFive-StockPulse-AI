package org.example.controller;

import org.example.model.Product;
import org.example.model.StockUpdate;
import org.example.repository.ProductRepository;
import org.example.repository.StockUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/corrections")
public class CorrectionController {

    @Autowired
    StockUpdateRepository stockUpdateRepository;

    @Autowired
    ProductRepository productRepository;

    @GetMapping("/stock-updates")
    public ResponseEntity<?> getUnresolvedStockUpdates() {
        List<StockUpdate> unresolved = stockUpdateRepository.findAll().stream()
                .filter(u -> "UNMATCHED".equals(u.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(unresolved);
    }

    @PutMapping("/stock-updates/{id}/resolve")
    public ResponseEntity<?> resolveStockUpdate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<StockUpdate> updateOpt = stockUpdateRepository.findById(id);
        if (updateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StockUpdate update = updateOpt.get();
        String correctProductName = body.get("productName");
        Integer correctQuantity = Integer.parseInt(body.get("quantity"));

        Optional<Product> productOpt = productRepository.findByNameIgnoreCase(correctProductName);
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No product found with that name. Create the product first.");
        }

        Product product = productOpt.get();
        product.setQuantity(product.getQuantity() + correctQuantity);
        productRepository.save(product);

        update.setMatchedProductName(correctProductName);
        update.setParsedQuantity(correctQuantity);
        update.setStatus("MATCHED");
        stockUpdateRepository.save(update);

        return ResponseEntity.ok(update);
    }
}