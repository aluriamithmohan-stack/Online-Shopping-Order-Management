package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC02: Search Product
 * Expected Result: Matching product displayed
 */
public class TC02_SearchProductTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC02: Search Product - Expected: Product displayed")
    public void testSearchProduct() {
        loginAsCustomer();

        WebElement searchBox = waitForVisible(By.id("searchBox"));
        searchBox.clear();
        searchBox.sendKeys("iPhone");

        clickById("searchBtn");

        waitForUrlContains("q=iPhone");
        assertTrue(pageContains("Apple iPhone 15 Pro"),
                "Search result should display 'Apple iPhone 15 Pro'");
        System.out.println("✅ TC02: Search product PASSED - Product displayed");
    }
}
