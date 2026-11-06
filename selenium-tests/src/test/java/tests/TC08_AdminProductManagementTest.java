package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TC08: Admin Product Management
 * Steps:
 *   1. Login as admin.
 *   2. Open the Add Product page.
 *   3. Fill in product details (name, description, price, stock, category).
 *   4. Submit and verify the product appears on the admin dashboard.
 *   5. Edit the product's price.
 *   6. Delete the product.
 * Expected: Admin can add, update and delete products successfully.
 */
public class TC08_AdminProductManagementTest extends BaseTest {

    private static final String TEST_PRODUCT_NAME = "Selenium Product " + System.currentTimeMillis();

    @BeforeEach
    public void setupTest() { super.setUp(); }

    @AfterEach
    public void tearDownTest() { super.tearDown(); }

    @Test
    @DisplayName("TC08: Admin Product Management - Expected: Admin can add, update and delete products")
    public void testAdminProductManagement() throws InterruptedException {
        // ── Step 1: Login as Admin ─────────────────────────────────────────
        loginAsAdmin();
        waitForUrlContains("/admin/dashboard");
        assertTrue(driver.getCurrentUrl().contains("/admin/dashboard"),
                "Admin should be redirected to dashboard after login");

        // ── Step 2: Open Add Product Page ─────────────────────────────────
        navigateTo("/admin/products/add");
        waitForVisible(By.id("saveProductBtn"));

        // ── Step 3: Fill in new product form ──────────────────────────────
        typeById("productName",        TEST_PRODUCT_NAME);
        typeById("productDescription", "Automated product description for testing");
        typeById("productPrice",       "999");
        typeById("productStock",       "50");
        selectByText("productCategory", "Electronics");

        clickById("saveProductBtn");

        // ── Step 4: Verify product appears on admin dashboard ─────────────
        waitForUrlContains("/admin/dashboard");
        assertTrue(pageContains(TEST_PRODUCT_NAME),
                "Newly added product should appear on the admin dashboard");

        // ── Step 5: Update the product's price ─────────────────────────────
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

        // ── Step 6: Delete the product ─────────────────────────────────────
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
