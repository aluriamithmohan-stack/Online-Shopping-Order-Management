package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC01: Customer Registration
 * Steps:
 *   1. Open the registration page.
 *   2. Enter a unique name, email, password and confirm password.
 *   3. Submit the registration form.
 * Expected: Registration should be successful and user redirected to login or products page.
 */
public class TC01_CustomerRegistrationTest extends BaseTest {

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC01: Customer Registration - Expected: Registration successful")
    public void testCustomerRegistration() {
        navigateTo("/auth/register");

        // Wait for the registration form to fully load
        waitForVisible(By.id("registerBtn"));

        // Fill in the registration form
        typeById("name",            "Selenium User");
        typeById("email",           NEW_EMAIL);
        typeById("password",        "Test@12345");
        typeById("confirmPassword", "Test@12345");

        clickById("registerBtn");

        // After successful registration the app redirects to login or products
        boolean redirectedToLogin    = driver.getCurrentUrl().contains("/auth/login");
        boolean redirectedToProducts = driver.getCurrentUrl().contains("/products");
        boolean successOnPage        = pageContains("success") || pageContains("Login") || pageContains("registered");

        assertTrue(redirectedToLogin || redirectedToProducts || successOnPage,
                "After registration the user should be redirected to login or products page");

        System.out.println("✅ TC01: Customer registration PASSED - Registration successful");
    }
}
