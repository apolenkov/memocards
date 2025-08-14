package org.apolenkov.application.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.testbench.TestBench;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

@org.junit.jupiter.api.Tag("ui")
class RegistrationPageIT {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = TestBench.createDriver(new ChromeDriver());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void registrationPageLoads() {
        driver.get("http://localhost:8080/register");
        String source = driver.getPageSource();
        assertThat(source).contains("Create your account");
        // Check form fields are present by host tags
        assertThat(driver.findElements(By.tagName("vaadin-text-field"))).isNotEmpty();
        assertThat(driver.findElements(By.tagName("vaadin-email-field"))).isNotEmpty();
        assertThat(driver.findElements(By.tagName("vaadin-password-field"))).isNotEmpty();
    }
}
