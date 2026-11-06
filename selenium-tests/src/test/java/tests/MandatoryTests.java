package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mandatory Selenium Test Cases (TC01 to TC08)
 * Project: Online Shopping and Order Management System (ShopEase)
 *
 * Test Cases:
 *   TC01 - Customer Registration
 *   TC02 - Product Catalogue
 *   TC03 - Product Search
 *   TC04 - Add to Cart
 *   TC05 - Update Cart Quantity
 *   TC06 - Checkout
 *   TC07 - Order History
 *   TC08 - Admin Product Management
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MandatoryTests extends BaseTest {

    // Unique email for registration test so it doesn't conflict across runs
    private static final String REG_EMAIL =
            "selenium_" + System.currentTimeMillis() + "@test.com";

    // Unique product name for admin management test
    private static final String TEST_PRODUCT_NAME =
            "Selenium Test Product " + System.currentTimeMillis();

    @BeforeEach
    public void init() { super.setUp(); }

    @AfterEach
    public void cleanup() { super.tearDown(); }

    // =========================================================================
    // TC01: CUSTOMER REGISTRATION
    // =========================================================================
    /**
     * Steps:
     *   1. Open registration page.
     *   2. Enter name, unique email, and password.
     *   3. Submit form.
     * Expected: User is redirected to login page or products page after successful registration.
     */
    @Test
    @Order(1)
    @DisplayName("TC01: Customer Registration")
    public void testCustomerRegistration() {
        navigateTo("/auth/register");

        // Wait for form to load
        waitForVisible(By.id("registerBtn"));

        typeById("name",            "Selenium User");
        typeById("email",           REG_EMAIL);
        typeById("password",        "Test@12345");
        typeById("confirmPassword", "Test@12345");

        clickById("registerBtn");

        boolean redirectedToLogin    = driver.getCurrentUrl().contains("/auth/login");
        boolean redirectedToProducts = driver.getCurrentUrl().contains("/products");
        boolean successOnPage        = pageContains("success") || pageContains("Login") || pageContains("registered");

        assertTrue(redirectedToLogin || redirectedToProducts || successOnPage,
                "After registration the user should be redirected to login or products page");

        System.out.println("✅ TC01: Customer registration PASSED");
    }

    // =========================================================================
    // TC02: PRODUCT CATALOGUE
    // =========================================================================
    /**
     * Steps:
     *   1. Login as customer.
     *   2. Open /products page.
     * Expected: At least one product card is displayed with price information.
     */
    @Test
    @Order(2)
    @DisplayName("TC02: Product Catalogue")
    public void testProductCatalogue() {
        loginAsCustomer();
        navigateTo("/products");

        WebElement heading = waitForVisible(By.tagName("h1"));
        assertTrue(heading.getText().contains("Product") || heading.getText().contains("Catalogue"),
                "Page heading should indicate the product catalogue");

        List<WebElement> cards = driver.findElements(By.cssSelector(".product-card"));
        assertFalse(cards.isEmpty(),
                "At least one product card should be displayed");

        assertTrue(pageContains("₹") || pageContains("$") || pageContains("Price"),
                "Product prices should be shown in the catalogue");

        System.out.println("✅ TC02: Product catalogue PASSED - " + cards.size() + " product(s) found");
    }

    // =========================================================================
    // TC03: PRODUCT SEARCH
    // =========================================================================
    /**
     * Steps:
     *   1. Login as customer.
     *   2. Enter "iPhone" in the search box.
     *   3. Submit search.
     * Expected: Matching products are displayed.
     */
    @Test
    @Order(3)
    @DisplayName("TC03: Product Search")
    public void testProductSearch() {
        loginAsCustomer();

        WebElement searchBox = waitForVisible(By.id("searchBox"));
        searchBox.clear();
        searchBox.sendKeys("iPhone");
        clickById("searchBtn");

        waitForUrlContains("q=iPhone");

        assertTrue(pageContains("Apple iPhone 15 Pro") || pageContains("iPhone"),
                "Search results should display products matching 'iPhone'");

        System.out.println("✅ TC03: Product search PASSED");
    }

    // =========================================================================
    // TC04: ADD TO CART
    // =========================================================================
    /**
     * Steps:
     *   1. Login as customer.
     *   2. Click Add to Cart on first product.
     *   3. Open cart.
     * Expected: Product is present in the cart.
     */
    @Test
    @Order(4)
    @DisplayName("TC04: Add to Cart")
    public void testAddToCart() {
        loginAsCustomer();
        addFirstProductToCart();
        navigateTo("/cart");

        WebElement cartTable = waitForVisible(By.cssSelector(".cart-table"));
        assertNotNull(cartTable, "Cart table should be visible after adding a product");

        assertTrue(pageContains("Proceed to Checkout") || pageContains("Order Summary"),
                "Cart should display checkout option");

        System.out.println("✅ TC04: Add to cart PASSED");
    }

    // =========================================================================
    // TC05: UPDATE CART QUANTITY
    // =========================================================================
    /**
     * Steps:
     *   1. Login and ensure one item is in cart.
     *   2. Set quantity to 2.
     *   3. Click Update Cart.
     * Expected: Quantity input reflects the value 2.
     */
    @Test
    @Order(5)
    @DisplayName("TC05: Update Cart Quantity")
    public void testUpdateCartQuantity() {
        loginAsCustomer();
        clearCart();
        addFirstProductToCart();
        navigateTo("/cart");

        WebElement qtyInput = waitForVisible(By.id("quantity"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2';", qtyInput);

        clickById("updateCartBtn");
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        WebElement updatedQty = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("quantity")));
        assertEquals("2", updatedQty.getAttribute("value"),
                "Cart quantity should be updated to 2");

        System.out.println("✅ TC05: Update cart quantity PASSED");
    }

    // =========================================================================
    // TC06: CHECKOUT
    // =========================================================================
    /**
     * Steps:
     *   1. Login, clear cart, add a product.
     *   2. Click Proceed to Checkout.
     *   3. Click Place Order.
     * Expected: Order confirmation page displayed with an ORD- prefixed order ID.
     */
    @Test
    @Order(6)
    @DisplayName("TC06: Checkout")
    public void testCheckout() {
        loginAsCustomer();
        clearCart();
        addFirstProductToCart();
        navigateTo("/cart");

        waitForVisible(By.id("checkoutBtn"));
        clickById("checkoutBtn");

        waitForUrlContains("/orders/checkout/review");
        clickById("placeOrderBtn");

        waitForUrlContains("/orders/confirmation");
        assertTrue(driver.getCurrentUrl().contains("/orders/confirmation"),
                "Should land on the order confirmation page");

        WebElement orderCode = waitForVisible(By.id("orderConfirmCode"));
        assertNotNull(orderCode, "Order ID must be shown on confirmation page");
        assertTrue(orderCode.getText().startsWith("ORD-"),
                "Order ID should start with 'ORD-'");

        System.out.println("✅ TC06: Checkout PASSED - Order: " + orderCode.getText());
    }

    // =========================================================================
    // TC07: ORDER HISTORY
    // =========================================================================
    /**
     * Steps:
     *   1. Login as customer.
     *   2. Navigate to /orders.
     * Expected: Order history page is displayed with orders or empty-state message.
     */
    @Test
    @Order(7)
    @DisplayName("TC07: Order History")
    public void testOrderHistory() {
        loginAsCustomer();
        navigateTo("/orders");

        waitForVisible(By.tagName("h1"));
        assertTrue(pageContains("My Orders") || pageContains("Order History"),
                "Page should contain 'My Orders' or 'Order History'");

        boolean hasContent = pageContains("ORD-") || pageContains("No orders") || pageContains("empty");
        assertTrue(hasContent,
                "Order history page should show orders or an empty-state message");

        System.out.println("✅ TC07: Order history PASSED");
    }

    // =========================================================================
    // TC08: ADMIN PRODUCT MANAGEMENT
    // =========================================================================
    /**
     * Steps:
     *   1. Login as admin.
     *   2. Add a new product via Add Product form.
     *   3. Verify product appears in list.
     *   4. Update the product's price.
     *   5. Delete the product.
     * Expected: Admin can add, update and delete products successfully.
     */
    @Test
    @Order(8)
    @DisplayName("TC08: Admin Product Management")
    public void testAdminProductManagement() throws InterruptedException {
        // Step 1: Login
        loginAsAdmin();
        waitForUrlContains("/admin/dashboard");
        assertTrue(driver.getCurrentUrl().contains("/admin/dashboard"),
                "Admin should be on dashboard");

        // Step 2 & 3: Add product
        navigateTo("/admin/products/add");
        waitForVisible(By.id("saveProductBtn"));

        typeById("productName",        TEST_PRODUCT_NAME);
        typeById("productDescription", "Automated product description for testing");
        typeById("productPrice",       "999");
        typeById("productStock",       "50");
        selectByText("productCategory", "Electronics");

        clickById("saveProductBtn");

        // Step 4: Verify on dashboard
        waitForUrlContains("/admin/dashboard");
        assertTrue(pageContains(TEST_PRODUCT_NAME),
                "Newly added product should appear on the admin dashboard");

        // Step 5: Update product
        WebElement editLink = driver.findElement(
                By.xpath("//tr[contains(., '" + TEST_PRODUCT_NAME + "')]//a[contains(@href, '/edit')]"));
        scrollIntoView(editLink);
        try {
            editLink.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editLink);
        }

        waitForVisible(By.id("productPrice"));
        WebElement priceField = driver.findElement(By.id("productPrice"));
        priceField.clear();
        priceField.sendKeys("1499");

        clickById("updateProductBtn");
        waitForUrlContains("/admin/dashboard");
        assertTrue(pageContains("1499") || pageContains("updated") || pageContains("success"),
                "Product price update should be reflected");

        // Step 6: Delete product
        autoAcceptConfirms();
        WebElement deleteBtn = driver.findElement(
                By.xpath("//tr[contains(., '" + TEST_PRODUCT_NAME + "')]//button[@id='deleteProductBtn']"));
        scrollIntoView(deleteBtn);
        try {
            deleteBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        }
        Thread.sleep(1000);

        navigateTo("/admin/dashboard");
        boolean productRemoved = !pageContains(TEST_PRODUCT_NAME) || pageContains("deleted");
        assertTrue(productRemoved,
                "Deleted product should no longer appear on dashboard");

        System.out.println("✅ TC08: Admin product management PASSED - Add, Update & Delete verified");
    }
}
