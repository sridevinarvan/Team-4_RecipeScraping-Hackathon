package pageObject;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import base.TestBase;

public class TarladalalSearch extends TestBase {

	public TarladalalSearch(WebDriver driver) {
		PageFactory.initElements(driver, this);
	}

	@SuppressWarnings("deprecation")
	public void scrapeAllRecipes(String foodCategory) throws Exception {

		JavascriptExecutor js = (JavascriptExecutor) driver;

		WebElement searchBox = driver.findElement(By.xpath("//input[@type='search' and @name='query']"));
		searchBox.sendKeys(foodCategory);

		WebElement Search = driver.findElement(By.xpath("//button[@class='btn btn-main primary-bg fw-semibold font-size-16 text-white search-btn w-100']//i[@class='fa fa-search']"));
		js.executeScript("arguments[0].click();", Search);

		System.out.println("Search " + foodCategory + " Foods");

		int total_recipes = 0;

		while (true) {
		    // Wait for page to load recipes
		    new WebDriverWait(driver, Duration.ofSeconds(5)).until(
		        ExpectedConditions.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0 two-line-text']/a"))
		    );

		    List<WebElement> allLinks = driver.findElements(By.xpath("//h5[@class='mb-0 two-line-text']/a"));
		    List<String> recipeNames = new ArrayList<>();
		    List<String> recipeUrls = new ArrayList<>();

		    for (WebElement link : allLinks) {
		        if (link.isDisplayed()) {
		            recipeNames.add(link.getText());
		            recipeUrls.add(link.getAttribute("href"));
		            total_recipes++;
		        }
		    }

		    // Scrape recipes
		    for (int j = 0; j < recipeUrls.size(); j++) {
		        String recipe_Name = recipeNames.get(j);
		        String recipe_URL = recipeUrls.get(j);
		        String recipe_ID = "";
		        String recipe_Category ="";
				String food_Category=""; 
				String ingredients="";
				String preparation_Time="";
				String cooking_Time="";
				String tag="";
				String no_of_servings="";
				String cuisine_category="";
				String recipe_Description="";
				String preparation_method="";
				String nutrient_values="";		
		        driver.navigate().to(recipe_URL);

		        // Extract ID
		        String id = recipe_URL.substring(recipe_URL.lastIndexOf("-") + 1);
		        recipe_ID = id.replaceAll("[^0-9]", ""); // Just digits

		        // Optional scraping logic (prep time, servings, etc.)
		        // Use try-catch if elements are optional
		        
		     // Preparation_Time
				preparation_Time = driver.findElement(By.xpath("//h6[text()='Preparation Time']/following-sibling::p/strong")).getText();
				
				// cooking time
				cooking_Time = driver.findElement(By.xpath("//h6[text()='Cooking Time']/following-sibling::p/strong")).getText();
				
				
				// No of servings
				WebElement noOfServingsEle = driver.findElement(By.xpath("//h6[text()='Makes ']/following-sibling::p/strong"));
				String Servings = noOfServingsEle.getText();
				
				// Removes everything except digits
				no_of_servings = Servings.replaceAll("[^0-9]", "");
				
				//tag
				WebElement tagItems = driver.findElement(By.cssSelector("div.col-md-12 ul.tags-list"));

				// Get the full visible text of the tag section
				tag = tagItems.getText();
				System.out.println("Entire Tags Text:\n" + tag);

		        System.out.println("Recipe Name scrapped: " + recipe_Name);
		        System.out.println("Recipe URL scrapped: " + recipe_URL);
		        System.out.println("Recipe Id scrapped: " + recipe_ID);
		        System.out.println("Number of servings is:" + no_of_servings);
				System.out.println("Preparation Time is:" + preparation_Time);
				System.out.println("Cooking Time is:" + cooking_Time);
				System.out.println("Tag : " + tag);
				
		       
				driver.navigate().back();

		        // Wait until page reloads before next scrape
		        new WebDriverWait(driver, Duration.ofSeconds(5)).until(
		            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0 two-line-text']/a"))
		        );
		    }

		    // Check for the "Next" button and click it
		    List<WebElement> nextButton = driver.findElements(By.xpath("//a[text()='Next']"));

		    if (!nextButton.isEmpty() && nextButton.get(0).isDisplayed()) {
		        js.executeScript("arguments[0].click();", nextButton.get(0));
		        Thread.sleep(2000); // Let the next page load
		    } else {
		        break;
		    }
		}

		System.out.println("Total number of " + foodCategory + " recipes scrapped are: " + total_recipes);
	}

}
