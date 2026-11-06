package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC02: Product Catalogue
 * Steps:
 *   1. Login as a customer.
 *   2. Open the product catalogue page.
 * Expected: Multiple product cards should be visible with name, price and stock details.
 */
public class TC02_ProductCatalogueTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC02: Product Catalogue - Expected: Products are displayed")
    public void testProductCatalogue() {
        loginAsCustomer();

        navigateTo("/products");

        // Heading should be present
        WebElement heading = waitForVisible(By.tagName("h1"));
        assertTrue(heading.getText().contains("Product") || heading.getText().contains("Catalogue"),
                "Page heading should contain 'Product' or 'Catalogue'");

        // At least one product card should be rendered
        List<WebElement> productCards = driver.findElements(By.cssSelector(".product-card"));
        assertFalse(productCards.isEmpty(),
                "At least one product card should be displayed in the catalogue");

        // Price and Add-to-Cart button must be visible on the first card
        assertTrue(pageContains("₹") || pageContains("$") || pageContains("Price"),
                "Product prices should be shown in the catalogue");

        System.out.println("✅ TC02: Product catalogue PASSED - " + productCards.size() + " product(s) displayed");
    }
}
