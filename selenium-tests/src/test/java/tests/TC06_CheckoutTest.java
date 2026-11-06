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
 * Steps:
 *   1. Login as a customer.
 *   2. Add a product to the cart.
 *   3. Click "Proceed to Checkout".
 *   4. Click "Place Order" on the review page.
 * Expected: Order confirmation page should be displayed with a valid Order ID (ORD-...).
 */
public class TC06_CheckoutTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC06: Checkout - Expected: Order placed successfully")
    public void testCheckout() {
        loginAsCustomer();
        clearCart();

        // Add a product and go to cart
        addFirstProductToCart();
        navigateTo("/cart");

        // Proceed to checkout
        waitForVisible(By.id("checkoutBtn"));
        clickById("checkoutBtn");

        // Review page
        waitForUrlContains("/orders/checkout/review");
        clickById("placeOrderBtn");

        // Confirmation page
        waitForUrlContains("/orders/confirmation");
        assertTrue(driver.getCurrentUrl().contains("/orders/confirmation"),
                "Should land on the order confirmation page");

        WebElement orderCode = waitForVisible(By.id("orderConfirmCode"));
        assertNotNull(orderCode, "Order ID must be shown on confirmation page");
        assertTrue(orderCode.getText().startsWith("ORD-"),
                "Order ID should start with 'ORD-'");

        System.out.println("✅ TC06: Checkout PASSED - Order placed (" + orderCode.getText() + ")");
    }
}
