package org.example.controller;

import org.example.model.Product;
import org.example.model.StockUpdate;
import org.example.repository.ProductRepository;
import org.example.repository.StockUpdateRepository;
import org.example.service.AIParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
public class StockUpdateController {

    @Autowired
    StockUpdateRepository stockUpdateRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    AIParsingService aiParsingService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitStockUpdate(@RequestBody Map<String, String> body) {
        String rawInput = body.get("rawInput");
        String submittedByEmail = body.get("submittedByEmail");

        StockUpdate update = new StockUpdate(rawInput, submittedByEmail);

        AIParsingService.ParsedStockResult parsed = aiParsingService.parseStockInput(rawInput);
        update.setParsedQuantity(parsed.getQuantity());
        update.setMatchedProductName(parsed.getProductName());

        if (parsed.getProductName() != null) {
            Optional<Product> productOpt = productRepository.findByNameIgnoreCase(parsed.getProductName());
            if (productOpt.isPresent() && parsed.getQuantity() != null) {
                Product product = productOpt.get();
                product.setQuantity(product.getQuantity() + parsed.getQuantity());
                productRepository.save(product);
                update.setStatus("MATCHED");
            } else {
                update.setStatus("UNMATCHED");
            }
        } else {
            update.setStatus("UNMATCHED");
        }

        StockUpdate saved = stockUpdateRepository.save(update);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> getAllStockUpdates() {
        return ResponseEntity.ok(stockUpdateRepository.findAll());
    }
}
