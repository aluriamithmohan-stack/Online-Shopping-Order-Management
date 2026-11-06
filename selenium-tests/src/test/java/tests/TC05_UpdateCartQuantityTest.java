package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TC05: Update Cart Quantity
 * Steps:
 *   1. Login and add a product to the cart.
 *   2. Open the cart page.
 *   3. Change the quantity input to 2.
 *   4. Click "Update Cart".
 * Expected: The cart should reflect the updated quantity of 2.
 */
public class TC05_UpdateCartQuantityTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC05: Update Cart Quantity - Expected: Cart updated with new quantity")
    public void testUpdateCartQuantity() {
        loginAsCustomer();
        clearCart();

        // Ensure there is at least one item in the cart
        addFirstProductToCart();
        navigateTo("/cart");

        // Set quantity via JavaScript to avoid clear/input issues
        WebElement qtyInput = waitForVisible(By.id("quantity"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2';", qtyInput);

        clickById("updateCartBtn");
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        // Re-fetch the input after update and verify
        WebElement updatedInput = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.id("quantity")));
        assertEquals("2", updatedInput.getAttribute("value"),
                "Cart quantity should be updated to 2");

        System.out.println("✅ TC05: Update cart quantity PASSED - Cart updated");
    }
}
