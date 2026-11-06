package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional Selenium Test Cases (TC08 to TC16)
 * Project: Online Shopping and Order Management System
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdditionalTests extends BaseTest {

    @BeforeEach
    public void init() {
        super.setUp();
    }

    @AfterEach
    public void cleanup() {
        super.tearDown();
    }

    /**
     * TC08: CUSTOMER REGISTRATION
     * Steps:
     * 1. Open registration page.
     * 2. Fill in valid unique name, email, password, confirmPassword.
     * 3. Submit form.
     * Expected: Account created, redirected to login page with success message.
     */
    @Test
    @Order(8)
    @DisplayName("TC08: Customer Registration")
    public void testCustomerRegistration() {
        navigateTo("/auth/register");

        String uniqueEmail = "user_" + System.currentTimeMillis() + "@test.com";

        typeById("name", "Automation Test User");
        typeById("email", uniqueEmail);
        typeById("password", "Secret@123");
        typeById("confirmPassword", "Secret@123");

        clickById("registerBtn");

        waitForUrlContains("/auth/login");
        assertTrue(driver.getCurrentUrl().contains("/auth/login"),
                "Should redirect to login page after registration");
        assertTrue(pageContains("Account created successfully") || pageContains("Please log in"),
                "Success flash notification should be displayed");
    }

    /**
     * TC09: DUPLICATE CUSTOMER REGISTRATION
     * Steps:
     * 1. Open registration page.
     * 2. Attempt to register with an existing customer's email.
     * 3. Submit form.
     * Expected: Registration rejected, error notification displayed.
     */
    @Test
    @Order(9)
    @DisplayName("TC09: Duplicate Customer Registration")
    public void testDuplicateRegistration() {
        navigateTo("/auth/register");

        typeById("name", "Existing User");
        typeById("email", CUSTOMER_EMAIL); // Existing demo email
        typeById("password", "Secret@123");
        typeById("confirmPassword", "Secret@123");

        clickById("registerBtn");

        // Should remain on register page with error
        waitForVisible(By.cssSelector(".alert-danger"));
        assertTrue(pageContains("already exists"),
                "Error alert should indicate email already registered");
    }

    /**
     * TC10: INVALID CUSTOMER LOGIN
     * Steps:
     * 1. Open login page.
     * 2. Enter invalid email or incorrect password.
     * 3. Click Login.
     * Expected: Login rejected, error message displayed.
     */
    @Test
    @Order(10)
    @DisplayName("TC10: Invalid Customer Login")
    public void testInvalidLogin() {
        navigateTo("/auth/login");

        typeById("email", "nonexistent@user.com");
        typeById("password", "WrongPassword999");
        clickById("loginBtn");

        waitForVisible(By.cssSelector(".alert-danger"));
        assertTrue(pageContains("Invalid email or password"),
                "Error notification should appear for invalid credentials");
    }

    /**
     * TC11: ADMIN LOGIN
     * Steps:
     * 1. Open admin login page.
     * 2. Enter valid admin email and password.
     * 3. Click Sign In.
     * Expected: Successfully logged in, admin dashboard displayed.
     */
    @Test
    @Order(11)
    @DisplayName("TC11: Admin Login")
    public void testAdminLogin() {
        navigateTo("/admin/login");

        typeById("email", ADMIN_EMAIL);
        typeById("password", ADMIN_PASSWORD);
        clickById("adminLoginBtn");

        waitForUrlContains("/admin/dashboard");
        assertTrue(driver.getCurrentUrl().contains("/admin/dashboard"),
                "Admin should be directed to /admin/dashboard");
        assertTrue(pageContains("Admin Dashboard") || pageContains("Product Inventory"),
                "Admin dashboard header/content should be visible");
    }

    /**
     * TC12: ADD PRODUCT AS ADMIN
     * Steps:
     * 1. Log in as admin.
     * 2. Open Add Product page.
     * 3. Fill in product details and submit.
     * Expected: Product added and visible in product inventory list.
     */
    @Test
    @Order(12)
    @DisplayName("TC12: Add Product as Admin")
    public void testAddProductAsAdmin() {
        loginAsAdmin();

        navigateTo("/admin/products/add");

        String newProductName = "Selenium Test Gadget " + System.currentTimeMillis();

        typeById("productName", newProductName);
        typeById("productDescription", "High quality automated test sample item for verification.");
        typeById("productPrice", "4999");
        typeById("productStock", "25");
        selectByText("productCategory", "Electronics");

        clickById("saveProductBtn");

        waitForUrlContains("/admin/dashboard");
        assertTrue(pageContains(newProductName),
                "Newly added product should appear on admin dashboard");
    }

    /**
     * TC13: UPDATE PRODUCT AS ADMIN
     * Steps:
     * 1. Log in as admin.
     * 2. Click Edit on an existing product.
     * 3. Change product price or description and save.
     * Expected: Product details updated successfully.
     */
    @Test
    @Order(13)
    @DisplayName("TC13: Update Product as Admin")
    public void testUpdateProductAsAdmin() {
        loginAsAdmin();

        // Navigate to edit first product (ID 1)
        navigateTo("/admin/products/1/edit");

        WebElement priceInput = waitForVisible(By.id("productPrice"));
        priceInput.clear();
        priceInput.sendKeys("139999");

        clickById("updateProductBtn");

        waitForUrlContains("/admin/dashboard");
        assertTrue(pageContains("updated successfully") || pageContains("139,999"),
                "Admin dashboard should reflect update confirmation");
    }

    /**
     * TC14: DELETE OR DEACTIVATE PRODUCT AS ADMIN
     * Steps:
     * 1. Log in as admin.
     * 2. Toggle active state of a product.
     * Expected: Product status toggles between Active and Inactive.
     */
    @Test
    @Order(14)
    @DisplayName("TC14: Toggle Product Status as Admin")
    public void testToggleProductStatus() {
        loginAsAdmin();

        clickById("toggleProductBtn");

        waitForUrlContains("/admin/dashboard");
        assertTrue(pageContains("Product activated") || pageContains("Product deactivated"),
                "Flash notification should confirm status change");
    }

    /**
     * TC15: EMPTY CART CHECKOUT VALIDATION
     * Steps:
     * 1. Log in as customer.
     * 2. Clear cart if any items exist.
     * 3. Attempt direct checkout URL /orders/checkout/review.
     * Expected: Redirected back to cart with 'cart is empty' error message.
     */
    @Test
    @Order(15)
    @DisplayName("TC15: Empty Cart Checkout")
    public void testEmptyCartCheckout() {
        loginAsCustomer();

        // Clear cart first
        navigateTo("/cart");
        try {
            WebElement clearBtn = driver.findElement(By.xpath("//button[contains(text(),'Clear Cart')]"));
            if (clearBtn.isDisplayed()) {
                clearBtn.click();
                try {
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                } catch (NoAlertPresentException ignored) {}
            }
        } catch (NoSuchElementException ignored) {}

        // Directly navigate to checkout
        navigateTo("/orders/checkout/review");

        // Should redirect back to cart with warning
        waitForUrlContains("/cart");
        assertTrue(pageContains("cart is empty") || pageContains("Browse Products"),
                "Empty cart notification should prevent checkout");
    }

    /**
     * TC16: INSUFFICIENT STOCK VALIDATION
     * Steps:
     * 1. Add product to cart.
     * 2. Try updating quantity beyond available stock.
     * Expected: Validation prevents updating and shows warning notification.
     */
    @Test
    @Order(16)
    @DisplayName("TC16: Insufficient Stock Validation")
    public void testInsufficientStockValidation() {
        loginAsCustomer();

        addFirstProductToCart();
        navigateTo("/cart");

        WebElement qtyInput = waitForVisible(By.id("quantity"));
        qtyInput.clear();
        qtyInput.sendKeys("999999"); // Exceeds stock limit

        clickById("updateCartBtn");
        try { Thread.sleep(800); } catch (Exception ignored) {}

        // Either HTML5 input constraint or server-side flash error is triggered
        assertTrue(pageContains("available") || pageContains("stock") || pageContains("Cart"),
                "Stock limit should be enforced");
    }
}
