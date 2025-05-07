package pages;

import java.io.IOException;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.*;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utilities.PostgresqlQueries;
import utilities.ExcelReader;

public class TarladalalSearch {

	List<String> LFV_EliminateItemList = new ArrayList<String>();
	List<String> LFV_AddItemList = new ArrayList<String>();

	List<String> LCHF_EliminateItemList = new ArrayList<String>();
	List<String> LCHF_AddItemList = new ArrayList<String>();
	List<String> foodCategoryDataList = new ArrayList<String>();
	List<String> cuisineDataList = new ArrayList<String>();

	List<String> recipeCategorieslist = new ArrayList<String>();

	WebDriver driver;
	WebDriverWait wait;
	JavascriptExecutor js;
	Connection conn;
	PostgresqlQueries dbQuries;

	public TarladalalSearch(WebDriver driver, Connection conn, PostgresqlQueries dbQuries) {
		this.driver = driver;
		this.conn = conn;
		this.dbQuries = dbQuries;
		PageFactory.initElements(driver, this);
		wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		this.js = (JavascriptExecutor) driver;
	}

	@SuppressWarnings("deprecation")
	public void scrapeAllRecipes() throws Exception {

		int LCHFCounter = 1;

		int LFVCounter = 1;

		// Read data from excel and store it into arraylist

		this.read_LFV_Data_Excel();

		this.read_LCHF_Data_Excel();

		this.read_FoodCategoryData_Excel();

		Map<String, Object[]> recipes_scrapped_treemap = new TreeMap<String, Object[]>();

		Map<String, Object[]> recipes_LCHF_Elimination = new TreeMap<String, Object[]>();

		Map<String, Object[]> recipes_LFV_Elimination = new TreeMap<String, Object[]>();

		Map<String, Object[]> recipes_LCHF_Add = new TreeMap<String, Object[]>();

		Map<String, Object[]> recipes_LFV_Add = new TreeMap<String, Object[]>();

		int foodcategoryCounter = 1;
		for (String foodCategory : foodCategoryDataList) {

			if (foodcategoryCounter > 1) {

				WebElement homeButton = driver
						.findElement(By.xpath("//a[@class='nav-link'][normalize-space()='Home']"));
				homeButton.click();
			}
			foodcategoryCounter++;
			int total_recipes = 0;

			JavascriptExecutor js = (JavascriptExecutor) driver;

			WebElement searchBox = driver.findElement(By.xpath("//input[@type='search' and @name='query']"));
			searchBox.sendKeys(foodCategory);

			WebElement Search = driver.findElement(By.xpath(
					"//button[@class='btn btn-main primary-bg fw-semibold font-size-16 text-white search-btn w-100']//i[@class='fa fa-search']"));
			js.executeScript("arguments[0].click();", Search);
			
			
			System.out.println("\n#####################################");
			System.out.println("Search " + foodCategory + " Foods");
			System.out.println("#####################################");
			
			WebElement resultText = driver.findElement(By.xpath("//p[contains(text(),'results for')]"));

			// Extract number using regex
			String text = resultText.getText(); 
			Matcher matcher = Pattern.compile("\\d+").matcher(text);
			if (matcher.find()) {
				System.out.println("\nNumber of "+foodCategory+" recipes are : " + matcher.group()); // prints 26
			}
			int number = Integer.parseInt(matcher.group());
			if (number > 0) {
				while (true) {

					// Wait for page to load recipes
					new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0 two-line-text']/a")));

					List<WebElement> allLinks = driver.findElements(By.xpath("//h5[@class='mb-0 two-line-text']/a"));
					List<String> recipeNames = new ArrayList<>();
					List<String> recipeUrls = new ArrayList<>();

					for (WebElement link : allLinks) {
						if (link.isDisplayed()) {

							/*if (total_recipes >= 2) {
								break;
							}*/
							recipeNames.add(link.getText());
							recipeUrls.add(link.getAttribute("href"));
							total_recipes++;
						}
					}
					//System.out.println("total recipes in " + foodCategory + " are :" + total_recipes);
					int recipeCounter = 0;

					// Scrape recipes
					for (int j = 0; j < recipeUrls.size(); j++) {

						recipeCounter++;// to store unique values into tree map

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

						// Preparation_Time
						try {
							preparation_Time = driver
									.findElement(
											By.xpath("//h6[text()='Preparation Time']/following-sibling::p/strong"))
									.getText();
						} catch (Exception e) {
						}
						// cooking time
						try {
							cooking_Time = driver
									.findElement(By.xpath("//h6[text()='Cooking Time']/following-sibling::p/strong"))
									.getText();
						} catch (Exception e) {
						}
						// No of servings
						try {
							WebElement noOfServingsEle = driver
									.findElement(By.xpath("//h6[text()='Makes ']/following-sibling::p/strong"));
							String Servings = noOfServingsEle.getText();

							// Removes everything except digits
							no_of_servings = Servings.replaceAll("[^0-9]", "");
						} catch (Exception e) {
						}
						// tag
						try {
							WebElement tagItems = driver.findElement(By.cssSelector("div.col-md-12 ul.tags-list"));

							// Get the full visible text of the tag section
							tags = tagItems.getText();
						} catch (Exception e) {
						}
						try {
							WebElement ingredientsSection = driver.findElement(By.xpath("//div[@id='ingredients']"));
							ingredients = ingredientsSection.getText();
						} catch (Exception e) {
						}

						for (String category : recipeCategorieslist) {
							if (tags.contains(category)) {
								recipe_Category = category;
								break;
							} else {
								recipe_Category = "not available";
							}
						}

						// Print cuisine categories
						for (String cuisine : cuisineDataList) {
							if (tags.contains(cuisine)) {
								cuisine_category = cuisine;
								break;

							} else {
								cuisine_category = "not available";
							}
						}
						try {
							WebElement recipedescriptionElement = driver
									.findElement(By.xpath("//p[contains(text(),'|')]"));
							recipe_Description = recipedescriptionElement.getText();
						} catch (Exception e) {
						}
						try {
							WebElement prepMethodElement = driver.findElement(By.xpath("//div[@id='methods']"));
							preparation_method = prepMethodElement.getText();
						} catch (Exception e) {
						}
						try {
							WebElement nutrientElement = driver.findElement(By.xpath("(//div[@id='rcpnuts'])[1]"));
							nutrient_values = nutrientElement.getText();
						} catch (Exception e) {
						}
						System.out.println(	"******************** " + recipe_Name + " **************************************");
						System.out.println("SNo: " + recipeCounter);
						System.out.println("Recipe Name scrapped: " + recipe_Name);
						System.out.println("Recipe URL scrapped: " + recipe_URL);
						System.out.println("Recipe Id scrapped: " + recipe_ID);
						
						boolean validLFVRecipe = true;
						// Retrieve data from Elimination arraylist using for loop,
						for (String eliminatedItem : LFV_EliminateItemList) {
							// Then compare each value with Ingredients.
							if (ingredients.contains(eliminatedItem) && eliminatedItem.trim() != "") {
								validLFVRecipe = false;
								break;
							}
						}

						if (validLFVRecipe) {
							recipes_LFV_Elimination.put(Integer.toString(LFVCounter),
									new Object[] { recipe_ID, recipe_Name, recipe_Category, food_Category, ingredients,
											preparation_Time, cooking_Time, tags, no_of_servings, cuisine_category,
											recipe_Description, preparation_method, nutrient_values, recipe_URL, "" });

							System.out.println("Valid LFV Item ");

							// Retrieve data from Add arraylist using for loop,
							for (String addItem : LFV_AddItemList) {
								// Then compare each value with Ingredients.
								if (ingredients.contains(addItem) && addItem.trim() != "") {
									System.out.println("Recipe contains LFV Add Item : " + addItem);

									recipes_LFV_Add.put(Integer.toString(LFVCounter),
											new Object[] { recipe_ID, recipe_Name, recipe_Category, food_Category,
													ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
													cuisine_category, recipe_Description, preparation_method,
													nutrient_values, recipe_URL, addItem });
									break;
								}
							}
							LFVCounter = LFVCounter + 1;
						}

						// Iterate LCHF Elimination array list using for loop and compare each value
						// with Ingredients to filter recipes
						boolean validLCHFRecipe = true;
						for (String eliminatedItem : LCHF_EliminateItemList) {

							if (ingredients.contains(eliminatedItem) && eliminatedItem.trim() != "") {
								validLCHFRecipe = false;
								break;
							}
						}

						if (validLCHFRecipe) {

							recipes_LCHF_Elimination.put(Integer.toString(LCHFCounter),
									new Object[] { recipe_ID, recipe_Name, recipe_Category, food_Category, ingredients,
											preparation_Time, cooking_Time, tags, no_of_servings, cuisine_category,
											recipe_Description, preparation_method, nutrient_values, recipe_URL, "" });

							System.out.println("Valid LCHF Recipe ");
							// System.out.println("Valid recipe Ingredients for LCHF " + ingredients);
							for (String addItem : LCHF_AddItemList) {

								if (ingredients.contains(addItem) && addItem.trim() != "") {
									System.out.println("Recipe contains LCHF Add Item : " + addItem);
									recipes_LCHF_Add.put(Integer.toString(LCHFCounter),
											new Object[] { recipe_ID, recipe_Name, recipe_Category, food_Category,
													ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
													cuisine_category, recipe_Description, preparation_method,
													nutrient_values, recipe_URL, addItem });

									break;
								}
							}

							LCHFCounter = LCHFCounter + 1;
						}

						recipes_scrapped_treemap.put(Integer.toString(recipeCounter),
								new Object[] { recipe_ID, recipe_Name, recipe_Category, food_Category, ingredients,
										preparation_Time, cooking_Time, tags, no_of_servings, cuisine_category,
										recipe_Description, preparation_method, nutrient_values, recipe_URL, "" });

						driver.navigate().back();

						// Wait until page reloads before next scrape
						// new WebDriverWait(driver, Duration.ofSeconds(10)).until(
						// ExpectedConditions.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0
						// two-line-text']/a")));
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
			}
			System.out.println("Total number of " + foodCategory + " recipes scrapped are: " + total_recipes);
		}
		// insert scrapped data into tables
		System.out.println("\n********************************************");
		System.out.println("Total Valid LFV Recipe(Elimination Check) = " + recipes_LFV_Elimination.size());
		System.out.println("Total Valid LFV Recipe(Add Check) = " + recipes_LFV_Add.size());

		System.out.println("Total Valid LCHF Recipe(Elimination Check) = " + recipes_LCHF_Elimination.size());
		System.out.println("Total Valid LCHF Recipe(Add Check) = " + recipes_LCHF_Add.size());
		System.out.println("********************************************");

		// write data to database

		// Insert data for LFV Elimination Recipes into PostgreSQL
		 dbQuries.insertRow(conn, "lfv_recipes_with_eliminateitems",recipes_LFV_Elimination);

		// Insert data for LFV Add Recipes into PostgreSQL
		 dbQuries.insertRow(conn, "lfv_recipes_with_addon_items", recipes_LFV_Add);

		// Insert data for LCHF Elimination Recipes into PostgreSQL
		 dbQuries.insertRow(conn, "lchf_recipes_with_eliminateitems",recipes_LCHF_Elimination);

		// Insert data for LCHF Add Recipes into PostgreSQL
		 dbQuries.insertRow(conn, "lchf_recipes_with_addon_items", recipes_LCHF_Add);

		 dbQuries.insertRow(conn, "recipes_scrapped_by_foodcategory",recipes_scrapped_treemap);
	}

	public void read_LFV_Data_Excel() {
		ExcelReader reader = new ExcelReader("./src/test/resources/IngredientsAndComorbidities-ScrapperHackathon.xlsx");
		Boolean sheetCheck = reader.isSheetExist("Final list for LFV Elimination ");
		System.out.println("Is the LFV Datasheet exist? -  " + sheetCheck);
		for (int i = 3; i <= 76; i++) {
			String testData = reader.getCellData("Final list for LFV Elimination ", 0, i);
			LFV_EliminateItemList.add(testData.toLowerCase());
			// System.out.println(testData);
		}
		for (int i = 3; i <= 90; i++) {
			String testData = reader.getCellData("Final list for LFV Elimination ", 1, i);
			LFV_AddItemList.add(testData.toLowerCase());
		}
	}

	public void read_LCHF_Data_Excel() {
		ExcelReader reader = new ExcelReader("./src/test/resources/IngredientsAndComorbidities-ScrapperHackathon.xlsx");
		Boolean sheetCheck = reader.isSheetExist("Final list for LCHFElimination ");
		System.out.println("Is the LCHF Datasheet exist? -  " + sheetCheck);
		for (int i = 3; i <= 92; i++) {
			String testData = reader.getCellData("Final list for LCHFElimination ", 0, i);
			LCHF_EliminateItemList.add(testData.toLowerCase());
		}

		for (int i = 3; i <= 34; i++) {
			String testData = reader.getCellData("Final list for LCHFElimination ", 1, i);
			LCHF_AddItemList.add(testData.toLowerCase());
		}
	}

	public void read_FoodCategoryData_Excel() {

		ExcelReader FoodCategoryreader = new ExcelReader("./src/test/resources/Recipe-filters-ScrapperHackathon.xlsx");
		Boolean sheetCheck1 = FoodCategoryreader.isSheetExist("Food Category");
		System.out.println("Is the Food Category Datasheet exist? -  " + sheetCheck1);

		for (int i = 2; i <= 6; i++) {
			String foodCategoryData = FoodCategoryreader.getCellData("Food Category", 0, i);
			foodCategoryDataList.add(foodCategoryData);
		}

		for (int f = 2; f <= 32; f++) {
			String cuisineData = FoodCategoryreader.getCellData("Food Category", 1, f);
			cuisineDataList.add(cuisineData);
		}
		for (int f = 2; f <= 32; f++) {
			String recipeCategories = FoodCategoryreader.getCellData("Food Category", 2, f);
			recipeCategorieslist.add(recipeCategories);
		}
	}
}
