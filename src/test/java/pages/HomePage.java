package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import utilities.ConfigReader;

public class HomePage {
	WebDriver driver;
    ConfigReader configReader;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.configReader = new ConfigReader();  // reads from config.properties
        PageFactory.initElements(driver, this);
    }

    public void open() {
        String url = configReader.getApplicationUrl();
        driver.get(url);
    }

}
