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
class PracticeFlowIT {

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
    void navigatePracticeView() {
        // Requires a running app with demo data; smoke-check the page opens
        driver.get("http://localhost:8080/practice/1");
        assertThat(driver.getPageSource()).contains("Practice");
        // show answer button exists
        assertThat(driver.findElements(By.xpath("//vaadin-button[contains(., '" + "Show answer" + "')]")))
                .isNotEmpty();
    }
}
