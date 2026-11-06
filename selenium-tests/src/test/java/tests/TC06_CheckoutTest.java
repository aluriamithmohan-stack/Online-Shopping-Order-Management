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
 * TC06: Checkout
 * Expected Result: Order placed
 */
public class TC06_CheckoutTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC06: Checkout - Expected: Order placed")
    public void testCheckout() {
        loginAsCustomer();
        clearCart();

        // Add product to cart
        addFirstProductToCart();

        // Open cart
        navigateTo("/cart");
        waitForVisible(By.id("checkoutBtn"));
        clickById("checkoutBtn");

        // Review & Confirm order
        waitForUrlContains("/orders/checkout/review");
        clickById("placeOrderBtn");

        // Verify confirmation page
        waitForUrlContains("/orders/confirmation");
        assertTrue(driver.getCurrentUrl().contains("/orders/confirmation"),
                "Should navigate to order confirmation page");

        WebElement codeEl = waitForVisible(By.id("orderConfirmCode"));
        assertNotNull(codeEl, "Order ID code should be displayed on confirmation");
        assertTrue(codeEl.getText().startsWith("ORD-"),
                "Order ID code should follow ORD- prefix pattern");
        System.out.println("✅ TC06: Checkout PASSED - Order placed (" + codeEl.getText() + ")");
    }
}
