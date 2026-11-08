package product;

import org.example.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void isLowStock_true_whenQuantityBelowThreshold() {
        Product product = new Product("Item", 2, 5);
        assertTrue(product.isLowStock());
    }

    @Test
    void isLowStock_true_whenQuantityEqualsThreshold() {
        Product product = new Product("Item", 5, 5);
        assertTrue(product.isLowStock());
    }

    @Test
    void isLowStock_false_whenQuantityAboveThreshold() {
        Product product = new Product("Item", 10, 5);
        assertFalse(product.isLowStock());
    }
}