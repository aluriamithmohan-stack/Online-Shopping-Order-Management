package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

/**
 * Base class for all ShopEase Selenium tests.
 * Provides common setup, teardown and robust browser automation utilities.
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // ── Application Configuration ──────────────────────────────────────────
    protected static final String BASE_URL =
            System.getProperty("baseUrl", "http://localhost:3000");

    // ── Demo Test Credentials ─────────────────────────────────────────────
    protected static final String CUSTOMER_EMAIL    = "demo@shopease.com";
    protected static final String CUSTOMER_PASSWORD = "Demo@123";
    protected static final String ADMIN_EMAIL       = "admin@shopease.com";
    protected static final String ADMIN_PASSWORD    = "Admin@123";

    // ── Generated unique email for registration tests ──────────────────────
    protected static final String NEW_EMAIL =
            "selenium_" + System.currentTimeMillis() + "@test.com";

    // ── Selenium test timeout ─────────────────────────────────────────────
    protected static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    protected static final Duration IMPLICIT_WAIT = Duration.ofSeconds(3);

    /**
     * Initialize ChromeDriver with Selenium Manager.
     */
    protected void setUp() {
        ChromeOptions options = new ChromeOptions();
        String headlessProp = System.getProperty("headless", "true");
        if (!"false".equalsIgnoreCase(headlessProp)) {
            options.addArguments("--headless=new");
        }
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1440,900");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    /**
     * Close the browser after each test.
     */
    protected void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
            driver = null;
        }
    }

    // ── Helper: Navigate to URL and auto-accept confirm prompts ─────────────
    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
        autoAcceptConfirms();
    }

    protected void autoAcceptConfirms() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "window.confirm = function() { return true; };" +
                "window.alert = function() { return true; };"
            );
        } catch (Exception ignored) {}
    }

    // ── Helper: Click element by ID with automatic JS fallback ──────────────
    protected void clickById(String id) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        scrollIntoView(el);
        autoAcceptConfirms();
        try {
            el.click();
        } catch (Exception ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    // ── Helper: Click element by CSS with automatic JS fallback ────────────
    protected void clickByCss(String css) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
        scrollIntoView(el);
        autoAcceptConfirms();
        try {
            el.click();
        } catch (Exception ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    // ── Helper: Scroll element into view ────────────────────────────────────
    protected void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'center'});", el);
            Thread.sleep(100);
        } catch (Exception ignored) {}
    }

    // ── Helper: Type into field by ID ───────────────────────────────────────
    protected void typeById(String id, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        scrollIntoView(el);
        el.clear();
        el.sendKeys(text);
    }

    // ── Helper: Wait for element visible ────────────────────────────────────
    protected WebElement waitForVisible(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(el);
        return el;
    }

    // ── Helper: Wait for element present ────────────────────────────────────
    protected WebElement waitForPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ── Helper: Check page title contains text ───────────────────────────────
    protected void waitForTitleContains(String text) {
        wait.until(ExpectedConditions.titleContains(text));
    }

    // ── Helper: Check URL contains text ─────────────────────────────────────
    protected void waitForUrlContains(String text) {
        wait.until(ExpectedConditions.urlContains(text));
    }

    // ── Helper: Login as customer ────────────────────────────────────────────
    protected void loginAsCustomer() {
        navigateTo("/auth/login");
        typeById("email", CUSTOMER_EMAIL);
        typeById("password", CUSTOMER_PASSWORD);
        clickById("loginBtn");
        waitForUrlContains("/products");
    }

    // ── Helper: Login as admin ───────────────────────────────────────────────
    protected void loginAsAdmin() {
        navigateTo("/admin/login");
        typeById("email", ADMIN_EMAIL);
        typeById("password", ADMIN_PASSWORD);
        clickById("adminLoginBtn");
        waitForUrlContains("/admin/dashboard");
    }

    // ── Helper: Logout ───────────────────────────────────────────────────────
    protected void logout() {
        navigateTo("/auth/logout");
    }

    // ── Helper: Clear customer cart ───────────────────────────────────────────
    protected void clearCart() {
        navigateTo("/cart");
        try {
            WebElement clearBtn = driver.findElement(By.xpath("//button[contains(text(),'Clear Cart')]"));
            if (clearBtn != null && clearBtn.isDisplayed()) {
                autoAcceptConfirms();
                try {
                    clearBtn.click();
                } catch (Exception ex) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clearBtn);
                }
                Thread.sleep(400);
            }
        } catch (Exception ignored) {}
    }

    // ── Helper: Add first available product to cart ───────────────────────────
    protected void addFirstProductToCart() {
        navigateTo("/products");
        WebElement addBtn = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[data-testid='addToCartBtn']")));
        scrollIntoView(addBtn);
        autoAcceptConfirms();
        try {
            addBtn.click();
        } catch (Exception ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        }
        try { Thread.sleep(600); } catch (Exception ignored) {}
    }

    // ── Helper: Check text exists on page ────────────────────────────────────
    protected boolean pageContains(String text) {
        return driver.getPageSource().contains(text);
    }

    // ── Helper: Get element text ─────────────────────────────────────────────
    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    // ── Helper: Select dropdown by visible text ───────────────────────────────
    protected void selectByText(String id, String visibleText) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
        scrollIntoView(el);
        new Select(el).selectByVisibleText(visibleText);
    }
}
