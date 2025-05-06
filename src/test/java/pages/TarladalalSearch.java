package pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utilities.ConfigReader;
import utilities.ExcelReader;

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
		System.out.println("Search " + foodCategory + " Foods");
		int total_recipes = 0;
		Map<String, Object[]> recipes_scrapped_treemap = new TreeMap<String, Object[]>();
		while (true) {
			
			// Wait for page to load recipes
			new WebDriverWait(driver, Duration.ofSeconds(5)).until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0 two-line-text']/a")));
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
				String recipe_Category = "";
				String food_Category = "";
				String ingredients = "";
				String preparation_Time = "";
				String cooking_Time = "";
				String tags = "";
				String no_of_servings = "";
				String cuisine_category = "";
				String recipe_Description = "";
				String preparation_method = "";
				String nutrient_values = "";
				driver.navigate().to(recipe_URL);
				// Extract ID
				String id = recipe_URL.substring(recipe_URL.lastIndexOf("-") + 1);
				recipe_ID = id.replaceAll("[^0-9]", ""); // Just digits
				// Optional scraping logic (prep time, servings, etc.)
				// Use try-catch if elements are optional
				// Preparation_Time
				preparation_Time = driver
						.findElement(By.xpath("//h6[text()='Preparation Time']/following-sibling::p/strong")).getText();
				// cooking time
				cooking_Time = driver.findElement(By.xpath("//h6[text()='Cooking Time']/following-sibling::p/strong"))
						.getText();
				// No of servings
				WebElement noOfServingsEle = driver
						.findElement(By.xpath("//h6[text()='Makes ']/following-sibling::p/strong"));
				String Servings = noOfServingsEle.getText();
				// Removes everything except digits
				no_of_servings = Servings.replaceAll("[^0-9]", "");
				// tag
				WebElement tagItems = driver.findElement(By.cssSelector("div.col-md-12 ul.tags-list"));
				// Get the full visible text of the tag section
				tags = tagItems.getText();
				WebElement ingredientsSection = driver.findElement(By.xpath("//div[@id='ingredients']"));
				ingredients = ingredientsSection.getText();
				
				food_Category = foodCategory;// from above
				List<String> recipeCategorieslist = new ArrayList<>(
						Arrays.asList("Breakfast", "Lunch", "Snack", "Dinner"));
				for (String category : recipeCategorieslist) {
					if (tags.contains(category)) {
						recipe_Category = category;
						System.out.println("Recipe Category: " + category);
						break;
					}
				}
				List<String> cuisineCategories = new ArrayList<>(Arrays.asList("Indian", "South Indian", "Rajathani",
						"Punjabi", "Bengali", "Orissa", "Gujarati", "Maharashtrian", "Andhra", "Kerala", "Goan",
						"Kashmiri", "Himachali", "Tamil Nadu", "Karnataka", "Sindhi", "Chhattisgarhi", "Madhya Pradesh",
						"Assamese", "Manipuri", "Tripuri", "Sikkimese", "Mizo", "Arunachali", "Uttarakhand", "Haryanvi",
						"Awadhi", "Bihari", "Uttar Pradesh", "Delhi", "North Indian"));
				// Print cuisine categories
				for (String cuisine : cuisineCategories) {
					if (tags.contains(cuisine)) {
						cuisine_category = cuisine;
						System.out.println("Cuisine Category: " + cuisine);
						break;
					}
				}
				try {
					WebElement recipedescriptionElement = driver.findElement(By.xpath("//p[contains(text(),'|')]"));
					recipe_Description = recipedescriptionElement.getText();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				try {
					WebElement prepMethodElement = driver.findElement(By.xpath("//div[@id='methods']"));
					preparation_method = prepMethodElement.getText();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				try {
					WebElement nutrientElement = driver.findElement(By.xpath("(//div[@id='rcpnuts'])[1]"));
					nutrient_values = nutrientElement.getText();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				System.out.println("******************** " + recipe_Name + " **************************************");
				System.out.println("Recipe Name scrapped: " + recipe_Name);
				System.out.println("Recipe URL scrapped: " + recipe_URL);
				System.out.println("Recipe Id scrapped: " + recipe_ID);
				System.out.println("Number of servings is:" + no_of_servings);
				System.out.println("Preparation Time is:" + preparation_Time);
				System.out.println("Cooking Time is:" + cooking_Time);
				System.out.println("Tag : " + tags);
				System.out.println("Ingredients:" + ingredients);
				System.out.println("Food category :" + food_Category);
				System.out.println("Recipe category :" + recipe_Category);
				System.out.println("cuisine category :" + cuisine_category);
				System.out.println("Recipe Description: " + recipe_Description);
				System.out.println("Preparation Method:\n" + preparation_method);
				System.out.println("Nutrient Values:\n" + nutrient_values);
				System.out.println("==========================");
				String filePath = ConfigReader.getExcelPath();
				ExcelReader.readFoodCategoriesFromExcel(filePath, ingredients);

				recipes_scrapped_treemap.put( Integer.toString(total_recipes) , new Object[] { recipe_ID, recipe_Name,
						  recipe_Category, food_Category, ingredients, preparation_Time,cooking_Time, tags,
						  no_of_servings, cuisine_category, recipe_Description,preparation_method, nutrient_values, recipe_URL,""});
				driver.navigate().back();
				// Wait until page reloads before next scrape
				new WebDriverWait(driver, Duration.ofSeconds(5)).until(
						ExpectedConditions.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0 two-line-text']/a")));
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
		// write data to database
		// Insert data for LFV Elimination Recipes into PostgreSQL
		
//		dbQuries.insertRow(conn, "recipes_scrapped_by_foodcategory",recipes_scrapped_treemap);
	}
}





