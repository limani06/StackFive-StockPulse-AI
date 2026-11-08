package org.example.repository;

import org.example.model.StockUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockUpdateRepository extends JpaRepository<StockUpdate, Long> {
}
