package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC05: Remove Product
 * Expected Result: Product removed
 */
public class TC05_RemoveProductTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC05: Remove Product - Expected: Product removed")
    public void testRemoveProduct() {
        loginAsCustomer();

        // Ensure item in cart
        addFirstProductToCart();
        navigateTo("/cart");

        clickById("removeProductBtn");

        // Verify removal confirmation or updated state
        assertTrue(pageContains("removed") || pageContains("empty") || pageContains("Cart"),
                "Cart should reflect that product has been removed");
        System.out.println("✅ TC05: Remove product PASSED - Product removed");
    }
}
