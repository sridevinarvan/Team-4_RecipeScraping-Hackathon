package testCases;

import org.testng.annotations.Test;

import base.TestBase;
import pages.TarladalalSearch;

public class TarlaDalalTest extends TestBase {
	
	@Test
    public void scrapeBreakfastRecipes() throws Exception {
		//ThreadLocal<WebDriver> driver = new ThreadLocal<>();
		TarladalalSearch searchscrape = new TarladalalSearch(driver.get());
		//searchscrape.scrapeAllRecipes("Vegan");
		//searchscrape.scrapeAllRecipes("Vegetarian");
		searchscrape.scrapeAllRecipes("Jain");
	}
}
        
       
       

