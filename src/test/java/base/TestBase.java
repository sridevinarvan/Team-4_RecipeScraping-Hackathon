package base;

import driverManager.DriverManager;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.safari.SafariDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.LoggerLoad;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public class TestBase {

	public static Properties prop;
	protected static WebDriver driver;

	public TestBase() {

		prop = new Properties();

		try {

			InputStream stream = TestBase.class.getClassLoader().getResourceAsStream("config.properties");
			prop.load(stream);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void initialization() {

		LoggerLoad.info("Inside init");
		LoggerLoad.info("The url is: " + prop.getProperty("url"));

		String browserName = prop.getProperty("browser").toLowerCase();

		switch (browserName) {
		case "chrome":
			ChromeOptions options = new ChromeOptions();
			options.addArguments("headless");
			driver = new ChromeDriver(options);
			driver.get(prop.getProperty("url"));
			System.out.println("Given URL for Recipe Scrapping : " + prop.getProperty("url"));
			System.out.println("Title : " + driver.getTitle());
			break;

		case "edge":
			WebDriverManager.edgedriver().setup();
			driver = new EdgeDriver();
			driver.manage().window().maximize();
			driver.manage().deleteAllCookies();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
			driver.get(prop.getProperty("url"));
			break;

		case "safari":
			WebDriverManager.safaridriver().setup();
			driver = new SafariDriver();
			driver.manage().window().maximize();
			driver.manage().deleteAllCookies();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
			driver.get(prop.getProperty("url"));
			break;

		default:
			throw new IllegalArgumentException("Unsupported browser: " + browserName);
		}
	}

	public String getTitleOfCurrentPage() {

		return driver.getTitle();
	}

	public String getURLOfCurrentPage() {

		return driver.getCurrentUrl();
	}

	public static WebDriver getDriver() {
		return driver;
	}

}
