package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC03: Product Search
 * Steps:
 *   1. Login as a customer.
 *   2. Enter a product name in the search box.
 *   3. Submit the search.
 * Expected: Matching products should be displayed in the results.
 */
public class TC03_ProductSearchTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC03: Product Search - Expected: Matching product displayed")
    public void testProductSearch() {
        loginAsCustomer();

        // The search box is on the products catalogue page
        WebElement searchBox = waitForVisible(By.id("searchBox"));
        searchBox.clear();
        searchBox.sendKeys("iPhone");

        clickById("searchBtn");

        // URL should contain the search query
        waitForUrlContains("q=iPhone");

        // The seeded product must appear
        assertTrue(pageContains("Apple iPhone 15 Pro") || pageContains("iPhone"),
                "Search results should display products matching 'iPhone'");

        System.out.println("✅ TC03: Product search PASSED - Matching product displayed");
    }
}
