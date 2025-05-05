package pages;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import utilities.LoggerLoad;

public class TarladalalSearch {
	
	WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;

    public TarladalalSearch(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.js = (JavascriptExecutor) driver;
    }

    @SuppressWarnings("deprecation")
	public void scrapeAllRecipes(String foodCategory) throws Exception {
    	JavascriptExecutor js = (JavascriptExecutor) driver;
		WebElement searchBox = driver.findElement(By.xpath("//input[@type='search' and @name='query']"));
		searchBox.sendKeys(foodCategory);
		WebElement Search = driver.findElement(By.xpath(
				"//button[@class='btn btn-main primary-bg fw-semibold font-size-16 text-white search-btn w-100']//i[@class='fa fa-search']"));
		js.executeScript("arguments[0].click();", Search);
		System.out.println("Search "+foodCategory+" Foods");
		// ------------------------------------------------------------------------------------//
		
		int total_recipes=0;
		
		while (true) {
			List<WebElement> allLinks = driver.findElements(By.xpath("//h5[@class='mb-0 two-line-text']/a"));
			// Filter only the visible ones			
			List<WebElement> recipeLinks = new ArrayList<>();
			int i = 0;
			for (WebElement link : allLinks) {
				if (link.isDisplayed()) {
					i++;
					total_recipes++;
					recipeLinks.add(link);
				}
			}
			
			//Scrape
			for (int j = 0; j < recipeLinks.size(); j++)
			{
				String id="";
				String recipe_ID="";
				String recipe_Name="";
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
				String recipe_URL="";
				
				WebElement recipeLink = recipeLinks.get(j);
				
				recipe_Name = recipeLink.getText();
				recipe_URL = recipeLink.getAttribute("href");
				id = recipe_URL.substring(recipe_URL.lastIndexOf("-")+1);
				
				recipe_ID = id.substring(0,id.length()-1);
				
				// Preparation_Time
				//preparation_Time = driver.findElement(By.xpath("//h6[text()='Preparation Time']/following-sibling::p/strong")).getText();
				
				// cooking time
				//cooking_Time = driver.findElement(By.xpath("//h6[text()='Cooking Time']/following-sibling::p/strong")).getText();
				
				
				// No of servings
				//WebElement noOfServingsEle = driver.findElement(By.xpath("//h6[text()='Makes ']/following-sibling::p/strong"));
				//String Servings = noOfServingsEle.getText();
				
				// Removes everything except digits
				//no_of_servings = Servings.replaceAll("[^0-9]", "");
				
				
			    System.out.println("Recipe Name scrapped: " + recipe_Name);
			    System.out.println("Recipe URL scrapped: " + recipe_URL);
			    System.out.println("Recipe Id scrapped: " + recipe_ID);
			 //   System.out.println("Number of servings is:" + no_of_servings);
				//System.out.println("Preparation Time is:" + preparation_Time);
				//System.out.println("Cooking Time is:" + cooking_Time);
			}
			
			// Check for the Next button and click if exists, else break
			List<WebElement> nextButton = driver.findElements(By.xpath("//a[text()='Next']"));
			if (nextButton.size() > 0 && nextButton.get(0).isDisplayed()) {				
				js.executeScript("arguments[0].click();", nextButton.get(0));
				Thread.sleep(2000); // Wait for new page to load
			} else {
				break; // Exit if no next page
			}
		}
		System.out.println("Total number of "+ foodCategory +" recipes scrapped are :" + total_recipes);
		
	}
}

	


