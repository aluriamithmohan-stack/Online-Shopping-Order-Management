package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC03: Add Product to Cart
 * Expected Result: Product added
 */
public class TC03_AddToCartTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC03: Add Product to Cart - Expected: Product added")
    public void testAddToCart() {
        loginAsCustomer();

        // Add product to cart
        addFirstProductToCart();

        // Open cart
        navigateTo("/cart");

        // Verify cart contains the added product
        WebElement cartTable = waitForVisible(By.cssSelector(".cart-table"));
        assertNotNull(cartTable, "Cart table should be visible");
        assertTrue(pageContains("Order Summary") || pageContains("Proceed to Checkout"),
                "Cart summary should display the added product");
        System.out.println("✅ TC03: Add product to cart PASSED - Product added");
    }
}
