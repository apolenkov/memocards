package org.apolenkov.application.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.testbench.BrowserTest;
import com.vaadin.testbench.TestBench;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.net.MalformedURLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@org.junit.jupiter.api.Tag("ui")
class LoginFlowIT {

    @BeforeEach
    void setUp() throws MalformedURLException {
        WebDriverManager.chromedriver().setup();
        driver = TestBench.createDriver(new ChromeDriver());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @BrowserTest
    void openLoginAndSeeForm() {
        driver.get("http://localhost:8080/login");
        assertThat(driver.getPageSource()).contains("Sign in");
        // smoke: username/password fields exist
        assertThat(driver.findElements(By.cssSelector("vaadin-login-form"))).isNotEmpty();
    }

    private WebDriver driver;
}
