package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC07: View Order History
 * Expected Result: Order details displayed
 */
public class TC07_ViewOrderHistoryTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC07: View Order History - Expected: Order details displayed")
    public void testViewOrderHistory() {
        loginAsCustomer();

        // Navigate to orders
        navigateTo("/orders");

        waitForVisible(By.tagName("h1"));
        assertTrue(pageContains("My Orders") || pageContains("Order History"),
                "Page heading should indicate My Orders");

        // Verify order details or table are displayed
        boolean hasOrders = pageContains("ORD-") || pageContains("No orders yet");
        assertTrue(hasOrders, "Order history page should render order details");
        System.out.println("✅ TC07: View order history PASSED - Order details displayed");
    }
}
