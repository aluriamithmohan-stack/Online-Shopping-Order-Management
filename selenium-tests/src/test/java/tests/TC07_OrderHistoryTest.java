package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC07: Order History
 * Steps:
 *   1. Login as a customer.
 *   2. Navigate to Order History (/orders).
 * Expected: Order history page should display existing orders or an appropriate empty-state message.
 */
public class TC07_OrderHistoryTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC07: Order History - Expected: Order history page displayed")
    public void testOrderHistory() {
        loginAsCustomer();

        navigateTo("/orders");

        // Page heading
        waitForVisible(By.tagName("h1"));
        assertTrue(pageContains("My Orders") || pageContains("Order History"),
                "Page heading should contain 'My Orders' or 'Order History'");

        // Orders table or empty-state message must be present
        boolean hasContent = pageContains("ORD-") || pageContains("No orders") || pageContains("empty");
        assertTrue(hasContent,
                "Order history page should show orders or an empty-state message");

        System.out.println("✅ TC07: Order history PASSED - Order history page displayed");
    }
}
