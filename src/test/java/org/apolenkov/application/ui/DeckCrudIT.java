package org.apolenkov.application.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaadin.testbench.BrowserTest;
import com.vaadin.testbench.TestBench;
import java.net.MalformedURLException;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

@Tag("ui")
class DeckCrudIT {

    private WebDriver driver;

    @BeforeEach
    void setUp() throws MalformedURLException {
        driver = TestBench.createDriver(new ChromeDriver());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @BrowserTest
    void createDeckAndAddCard() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.get("http://localhost:8080/login");

        // login as dev user
        WebElement form = driver.findElement(By.cssSelector("vaadin-login-form"));
        form.findElement(By.cssSelector("input[name='username']")).sendKeys("user");
        form.findElement(By.cssSelector("input[name='password']")).sendKeys("Password1");
        form.findElement(By.cssSelector("button[type='submit']")).click();

        // on home, click Add Deck
        WebElement addDeckBtn = driver.findElement(By.cssSelector("vaadin-button[data-testid='home-add-deck']"));
        addDeckBtn.click();

        // dialog: enter title and save
        driver.findElement(By.xpath("//vaadin-text-field//input")).sendKeys("UI Deck");
        driver.findElement(By.xpath("//vaadin-button[contains(., 'Create')]")).click();

        // navigate landed deck view, click Add Card
        driver.findElement(By.cssSelector("vaadin-button[data-testid='deck-add-card']"))
                .click();
        var inputs = driver.findElements(By.cssSelector("vaadin-text-field input"));
        inputs.get(0).sendKeys("front");
        inputs.get(1).sendKeys("back");
        driver.findElement(By.xpath("//vaadin-button[contains(., 'Save')]")).click();

        // a grid should contain 'front'
        assertThat(driver.getPageSource()).contains("front");
    }
}
