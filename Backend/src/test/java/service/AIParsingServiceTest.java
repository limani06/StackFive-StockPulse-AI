package service;

import org.example.service.AIParsingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIParsingServiceTest {

    private final AIParsingService service = new AIParsingService();

    @Test
    void parsesQuantityAndProductName_whenBothPresent() {
        AIParsingService.ParsedStockResult result = service.parseStockInput("Received 10 Rice 10kg");

        assertEquals(10, result.getQuantity());
        assertEquals("Rice 10kg", result.getProductName());
    }

    @Test
    void parsesSimpleInput_withNoNumberInProductName() {
        AIParsingService.ParsedStockResult result = service.parseStockInput("Received 5 Sugar");

        assertEquals(5, result.getQuantity());
        assertEquals("Sugar", result.getProductName());
    }

    @Test
    void returnsNulls_whenInputIsBlank() {
        AIParsingService.ParsedStockResult result = service.parseStockInput("");

        assertNull(result.getQuantity());
        assertNull(result.getProductName());
    }

    @Test
    void returnsNullQuantity_whenNoNumberPresent() {
        AIParsingService.ParsedStockResult result = service.parseStockInput("Rice delivery arrived");

        assertNull(result.getQuantity());
        assertNotNull(result.getProductName());
    }
}
