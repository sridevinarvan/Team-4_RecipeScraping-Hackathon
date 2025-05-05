package base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import driverManager.DriverManager;
import utilities.LoggerLoad;

public class TestBase extends DriverManager {
	
	@Parameters({ "browser", "headless" })
	@BeforeMethod
	public void setUp(@Optional("chrome") String browser, @Optional("true") String headlessParam) {
		LoggerLoad.info("Loading the driver in " + browser + ", Headless mode: " + headlessParam);
		boolean headless = Boolean.parseBoolean(headlessParam);
		// Set the URL for navigation
		String url = configReader.getApplicationUrl();
		LoggerLoad.info("inside testbase class");
		DriverManager.createDriver(browser, headless);
		WebDriver driver = DriverManager.getDriver();
		if (driver == null) {
			LoggerLoad.info("ERROR: WebDriver is null for \" + browser");
		} else {
			LoggerLoad.info("Getting inside driver");
			driver.get(url);
			LoggerLoad.info("Inside testBase url::" + url);
		}
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(configReader.getImplicitlyWait()));
		// driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

	}

	@AfterMethod
	public void tearDown() {

		LoggerLoad.info("-------------------------------------------------------");
		DriverManager.quitDriver();

	}

}
