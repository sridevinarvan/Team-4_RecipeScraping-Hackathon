package tests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import base.TestBase;
import pageObject.TarladalalSearch;
import pages.*;

public class HomePageTest extends TestBase {

	HomePage homePage;

	public HomePageTest() {
		super();
	}

	@BeforeClass
	public void setup() {

		TestBase.initialization();
		homePage = new HomePage();
	}
	
	@Test(priority=1)
	public void scrapeAllRecipes() throws Exception {		
	
		TarladalalSearch searchscrape = new TarladalalSearch(TestBase.getDriver());
		searchscrape.scrapeAllRecipes("Vegan");
		//searchscrape.scrapeAllRecipes("Vegetarian");
		//searchscrape.scrapeAllRecipes("Jain");
	}	

	@AfterClass
	public void teardown() throws InterruptedException {
		Thread.sleep(3000);
		driver.quit();
	}
}
