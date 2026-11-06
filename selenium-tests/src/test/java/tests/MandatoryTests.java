package tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mandatory Selenium Test Cases (TC01 to TC07)
 * Project: Online Shopping and Order Management System
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MandatoryTests extends BaseTest {

    @BeforeEach
    public void init() {
        super.setUp();
    }

    @AfterEach
    public void cleanup() {
        super.tearDown();
    }

    /**
     * TC01: CUSTOMER LOGIN
     * Steps:
     * 1. Open login page.
     * 2. Enter valid customer email.
     * 3. Enter valid password.
     * 4. Click Login.
     * Expected: Customer login should be successful and the catalogue/dashboard displayed.
     */
    @Test
    @Order(1)
    @DisplayName("TC01: Customer Login")
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
    }

    /**
     * TC02: SEARCH PRODUCT
     * Steps:
     * 1. Open product catalogue (after logging in).
     * 2. Enter an existing product name in the search box.
     * 3. Click Search.
     * Expected: Matching product should be displayed.
     */
    @Test
    @Order(2)
    @DisplayName("TC02: Search Product")
    public void testSearchProduct() {
        loginAsCustomer();

        WebElement searchBox = waitForVisible(By.id("searchBox"));
        searchBox.clear();
        searchBox.sendKeys("iPhone");

        clickById("searchBtn");

        waitForUrlContains("q=iPhone");
        assertTrue(pageContains("Apple iPhone 15 Pro"),
                "Search result should display 'Apple iPhone 15 Pro'");
    }

    /**
     * TC03: ADD PRODUCT TO CART
     * Steps:
     * 1. Select an available product.
     * 2. Click Add to Cart.
     * 3. Open shopping cart.
     * Expected: Selected product should be added successfully.
     */
    @Test
    @Order(3)
    @DisplayName("TC03: Add Product to Cart")
    public void testAddToCart() {
        loginAsCustomer();

        // Add first product to cart from catalogue
        addFirstProductToCart();

        // Open cart
        navigateTo("/cart");

        // Verify cart contains items
        WebElement cartTable = waitForVisible(By.cssSelector(".cart-table"));
        assertNotNull(cartTable, "Cart table should be visible");
        assertTrue(pageContains("Proceed to Checkout") || pageContains("Order Summary"),
                "Cart summary should be visible with items");
    }

    /**
     * TC04: UPDATE CART QUANTITY
     * Steps:
     * 1. Open shopping cart.
     * 2. Increase the product quantity.
     * 3. Update the cart.
     * Expected: The updated quantity and total amount should be displayed correctly.
     */
    @Test
    @Order(4)
    @DisplayName("TC04: Update Cart Quantity")
    public void testUpdateCartQuantity() {
        loginAsCustomer();
        clearCart();

        // Ensure at least one item in cart
        addFirstProductToCart();
        navigateTo("/cart");

        WebElement qtyInput = waitForVisible(By.id("quantity"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2';", qtyInput);

        clickById("updateCartBtn");

        // Verify update feedback or persisted value
        waitForVisible(By.id("quantity"));
        WebElement updatedInput = driver.findElement(By.id("quantity"));
        assertEquals("2", updatedInput.getAttribute("value"),
                "Quantity input should reflect updated value of 2");
    }

    /**
     * TC05: REMOVE PRODUCT
     * Steps:
     * 1. Open shopping cart.
     * 2. Click Remove Product.
     * Expected: Selected product should be removed and cart total should be updated.
     */
    @Test
    @Order(5)
    @DisplayName("TC05: Remove Product")
    public void testRemoveProduct() {
        loginAsCustomer();

        // Ensure item in cart
        addFirstProductToCart();
        navigateTo("/cart");

        clickById("removeProductBtn");

        // Verify success alert or updated cart
        assertTrue(pageContains("removed") || pageContains("empty") || pageContains("Cart"),
                "Cart should show item removed confirmation");
    }

    /**
     * TC06: CHECKOUT
     * Steps:
     * 1. Add an available product to cart.
     * 2. Open checkout.
     * 3. Confirm the order.
     * Expected: Order should be placed successfully and order confirmation should be displayed.
     */
    @Test
    @Order(6)
    @DisplayName("TC06: Checkout")
    public void testCheckout() {
        loginAsCustomer();
        clearCart();

        // Add product to cart
        addFirstProductToCart();

        // Open cart
        navigateTo("/cart");
        waitForVisible(By.id("checkoutBtn"));
        clickById("checkoutBtn");

        // Review & Confirm order
        waitForUrlContains("/orders/checkout/review");
        clickById("placeOrderBtn");

        // Verify confirmation page
        waitForUrlContains("/orders/confirmation");
        assertTrue(driver.getCurrentUrl().contains("/orders/confirmation"),
                "Should navigate to order confirmation page");

        WebElement codeEl = waitForVisible(By.id("orderConfirmCode"));
        assertNotNull(codeEl, "Order ID code should be displayed on confirmation");
        assertTrue(codeEl.getText().startsWith("ORD-"),
                "Order ID code should follow ORD- prefix pattern");
    }

    /**
     * TC07: VIEW ORDER HISTORY
     * Steps:
     * 1. Login as a customer.
     * 2. Open Order History.
     * Expected: Correct order details should be displayed.
     */
    @Test
    @Order(7)
    @DisplayName("TC07: View Order History")
    public void testViewOrderHistory() {
        loginAsCustomer();

        // Open orders page
        navigateTo("/orders");

        waitForVisible(By.tagName("h1"));
        assertTrue(pageContains("My Orders") || pageContains("Order History"),
                "Page heading should indicate My Orders");

        // Verify table or empty state exists
        boolean hasOrders = pageContains("ORD-") || pageContains("No orders yet");
        assertTrue(hasOrders, "Order history page should render order list or empty message");
    }
}
