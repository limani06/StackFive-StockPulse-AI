package org.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_updates")
public class StockUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String rawInput;

    private String matchedProductName;

    private Integer parsedQuantity;

    @Column(nullable = false)
    private String status; // "MATCHED" or "UNMATCHED"

    private String submittedByEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public StockUpdate() {
        this.createdAt = LocalDateTime.now();
    }

    public StockUpdate(String rawInput, String submittedByEmail) {
        this.rawInput = rawInput;
        this.submittedByEmail = submittedByEmail;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }

    public String getMatchedProductName() { return matchedProductName; }
    public void setMatchedProductName(String matchedProductName) { this.matchedProductName = matchedProductName; }

    public Integer getParsedQuantity() { return parsedQuantity; }
    public void setParsedQuantity(Integer parsedQuantity) { this.parsedQuantity = parsedQuantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmittedByEmail() { return submittedByEmail; }
    public void setSubmittedByEmail(String submittedByEmail) { this.submittedByEmail = submittedByEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
