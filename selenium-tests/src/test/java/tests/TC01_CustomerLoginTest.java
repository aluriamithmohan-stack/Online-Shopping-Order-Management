package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC01: Customer Login
 * Expected Result: Login successful
 */
public class TC01_CustomerLoginTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        super.setUp();
    }

    @AfterEach
    public void tearDownTest() {
        super.tearDown();
    }

    @Test
    @DisplayName("TC01: Customer Login - Expected: Login successful")
    public void testCustomerLogin() {
        navigateTo("/auth/login");

        typeById("email", CUSTOMER_EMAIL);
        typeById("password", CUSTOMER_PASSWORD);
        clickById("loginBtn");

        waitForUrlContains("/products");
        assertTrue(driver.getCurrentUrl().contains("/products"),
                "User should be redirected to /products after successful login");

        WebElement heading = waitForVisible(By.tagName("h1"));
        assertTrue(heading.getText().contains("Product Catalogue") || heading.getText().contains("Products"),
                "Product Catalogue heading should be displayed");
        System.out.println("✅ TC01: Customer login PASSED - Login successful");
    }
}
