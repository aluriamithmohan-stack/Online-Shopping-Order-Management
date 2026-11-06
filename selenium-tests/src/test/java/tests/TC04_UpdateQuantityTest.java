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
 * TC04: Update Quantity
 * Expected Result: Cart updated
 */
public class TC04_UpdateQuantityTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC04: Update Quantity - Expected: Cart updated")
    public void testUpdateQuantity() {
        loginAsCustomer();
        clearCart();

        // Ensure item in cart
        addFirstProductToCart();
        navigateTo("/cart");

        WebElement qtyInput = waitForVisible(By.id("quantity"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2';", qtyInput);

        clickById("updateCartBtn");

        // Verify updated quantity
        waitForVisible(By.id("quantity"));
        WebElement updatedInput = driver.findElement(By.id("quantity"));
        assertEquals("2", updatedInput.getAttribute("value"),
                "Quantity input should reflect updated value of 2");
        System.out.println("✅ TC04: Update quantity PASSED - Cart updated");
    }
}
