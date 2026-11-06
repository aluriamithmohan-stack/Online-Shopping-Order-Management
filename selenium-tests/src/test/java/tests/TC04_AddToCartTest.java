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
 * TC04: Add to Cart
 * Steps:
 *   1. Login as a customer.
 *   2. Navigate to the product catalogue.
 *   3. Click "Add to Cart" on the first available product.
 *   4. Open the shopping cart.
 * Expected: The selected product should be present in the cart.
 */
public class TC04_AddToCartTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC04: Add to Cart - Expected: Product added to cart")
    public void testAddToCart() {
        loginAsCustomer();

        // Click Add to Cart on the first product
        addFirstProductToCart();

        // Open cart page
        navigateTo("/cart");

        // Cart table must be visible
        WebElement cartTable = waitForVisible(By.cssSelector(".cart-table"));
        assertNotNull(cartTable, "Cart table should be visible after adding a product");

        assertTrue(pageContains("Proceed to Checkout") || pageContains("Order Summary"),
                "Cart should display checkout option after adding a product");

        System.out.println("✅ TC04: Add to cart PASSED - Product added to cart");
    }
}
