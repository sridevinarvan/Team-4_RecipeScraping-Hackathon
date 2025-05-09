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

import utilities.ExcelReader;
import utilities.PostgresqlQueries;

public class Search_Scrape_Recipes {

	private final WebDriver driver;
	private final WebDriverWait wait;
	private final JavascriptExecutor js;
	private final Connection conn;
	private final PostgresqlQueries dbQuery;

	private final List<String> LFV_EliminateItemList = new ArrayList<>();
	private final List<String> LFV_AddItemList = new ArrayList<>();
	private final List<String> LCHF_EliminateItemList = new ArrayList<>();
	private final List<String> LCHF_AddItemList = new ArrayList<>();
	private final List<String> foodCategoryDataList = new ArrayList<>();
	private final List<String> cuisineDataList = new ArrayList<>();
	private final List<String> recipeCategorieslist = new ArrayList<>();
	private final List<String> allergiesList = new ArrayList<>();

	public Search_Scrape_Recipes(WebDriver driver, Connection conn, PostgresqlQueries dbQuries) {
		this.driver = driver;
		this.conn = conn;
		this.dbQuery = dbQuries;
		PageFactory.initElements(driver, this);
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		this.js = (JavascriptExecutor) driver;
	}

	@SuppressWarnings("deprecation")
	public void scrapeAllRecipes() throws Exception {

		int LCHFCounter = 1, LFVCounter = 1, all_recipesCounter = 1;

		// Read data from excel and store it into arraylist
		readExcelData();

		// to store scrapped recipe values
		Map<String, Object[]> recipes_scrapped_treemap = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_LCHF_Elimination = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_LFV_Elimination = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_LCHF_Add = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_LFV_Add = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_Allergy_Milk = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_Allergy_Nut = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_LCHF_Allergy_Milk = new TreeMap<String, Object[]>();
		Map<String, Object[]> recipes_LCHF_Allergy_Nut = new TreeMap<String, Object[]>();
		//int total_recipes = 0;// counter for total recipes
		for (int foodCatIndex = 0; foodCatIndex < foodCategoryDataList.size(); foodCatIndex++) {

			String foodCategory = foodCategoryDataList.get(foodCatIndex);
			if (foodCatIndex > 0) {
				WebElement homeButton = driver
						.findElement(By.xpath("//a[@class='nav-link'][normalize-space()='Home']"));
				homeButton.click();
			}
			System.out.println("\n#####################################");
			System.out.println("Search " + foodCategory + " Foods");
			System.out.println("#####################################");

			searchFood(foodCategory);
			int recipeCounter = 0;
			int totalRecipes = getRecipeCount();

			System.out.println("\nNumber of " + foodCategory + " recipes to scrape are : " + totalRecipes); // prints 26

			if (totalRecipes > 0) {

				do {
					// Wait for page to load recipes
					new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("//h5[@class='mb-0 two-line-text']/a")));

					List<WebElement> allLinks = driver.findElements(By.xpath("//h5[@class='mb-0 two-line-text']/a"));
					List<String> recipeNames = new ArrayList<>();
					List<String> recipeUrls = new ArrayList<>();
					
					for (WebElement link : allLinks) {
						if (link.isDisplayed()) {
							recipeNames.add(link.getText());
							recipeUrls.add(link.getAttribute("href"));
							all_recipesCounter++;		
							//total_recipes++;							
						}
					}

					// Scrape recipes
					for (int j = 0; j < recipeUrls.size(); j++) {

						recipeCounter++;// counter to store unique values into tree map

						String recipe_Name = recipeNames.get(j), recipe_URL = recipeUrls.get(j), recipe_ID = "",
								recipe_Category = "", ingredients = "", preparation_Time = "", cooking_Time = "",
								tags = "", no_of_servings = "", cuisine_category = "", recipe_Description = "",
								preparation_method = "", nutrient_values = "";
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
						List<WebElement> tagElements = driver
								.findElements(By.cssSelector("div.col-md-12 ul.tags-list"));
						if (!tagElements.isEmpty()) {
							tags = tagElements.get(0).getText();
						} else {
							tags = "not available"; 
						}

						List<WebElement> ingredientsSectionList = driver
								.findElements(By.xpath("//div[@id='ingredients']"));

						if (!ingredientsSectionList.isEmpty()) {
							ingredients = ingredientsSectionList.get(0).getText();
						} else {
							ingredients = "not available";
						}

					
						for (String category : recipeCategorieslist) {
							if (tags.contains(category)) {
								recipe_Category = category;
								break;
							}
						}

						// Print cuisine categories
						cuisine_category = "not available";
						for (String cuisine : cuisineDataList) {
							if (tags.contains(cuisine)) {
								cuisine_category = cuisine;
								break;
							}
						}
						
						try {
							WebElement recipedescriptionElement = driver.findElement(By.xpath("//*[@id='aboutrecipe']/p[1]"));
							recipe_Description = recipedescriptionElement.getText();
						} catch (Exception e) {
						}
						
						List<WebElement> prepMethodElements = driver.findElements(By.xpath("//div[@id='methods']"));
						if (!prepMethodElements.isEmpty()) {
							preparation_method = prepMethodElements.get(0).getText();
						} else {
							preparation_method = "not available";
						}

						List<WebElement> nutrientElements = driver.findElements(By.xpath("(//div[@id='rcpnuts'])[1]"));
						if (!nutrientElements.isEmpty()) {
							nutrient_values = nutrientElements.get(0).getText();
						} else {
							nutrient_values = "not available";
						}

						System.out.println(
								"******************** " + recipe_Name + " **************************************");
						System.out.println("SNo: " + recipeCounter);
						System.out.println("Recipe URL scrapped: " + recipe_URL);
						System.out.println("Recipe Id scrapped: " + recipe_ID);

						// ***** Iterate LFV Elimination array list using for loop and compare each
						// value *****
						// ****** with Ingredients to filter recipes*****

						boolean validLFVRecipe = true;

						for (String eliminatedItem : LFV_EliminateItemList) {
							if (ingredients.contains(eliminatedItem) && eliminatedItem.trim() != "") {
								validLFVRecipe = false;
								break;
							}
						}

						if (validLFVRecipe) {

							System.out.println("Valid LFV Item ");
							// Add
							recipes_LFV_Elimination.put(Integer.toString(LFVCounter),
									new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory, ingredients,
											preparation_Time, cooking_Time, tags, no_of_servings, cuisine_category,
											recipe_Description, preparation_method, nutrient_values, recipe_URL, "" });

							// check add on items
							boolean addLFVItemcheck = false;
							String addLFVItemsList = "";
							for (String addItem : LFV_AddItemList) {
								if (ingredients.contains(addItem.trim()) && addItem.trim() != "") {
									if (addLFVItemsList.trim() == "")
										addLFVItemsList = addItem;
									else
										addLFVItemsList += "," + addItem;
									addLFVItemcheck = true;
								}
							}
							if (addLFVItemcheck) {
								System.out.println("Recipe contains LFV Add Items : " + addLFVItemsList);
								
								recipes_LFV_Add.put("LFV :" + Integer.toString(LFVCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, addLFVItemsList });
							}
							// code check LFV allergens
							boolean allergyMilk = false;
							boolean allergyNut = false;
							String addLFVAllergensItemsList = "";
							for (String allergyItem : allergiesList) {
								String trimmedItem = allergyItem.trim();
								if (!trimmedItem.isEmpty()
										&& ingredients.toLowerCase().contains(trimmedItem.toLowerCase())) {
									if (trimmedItem.equalsIgnoreCase("milk")) {

										allergyMilk = true;
									} else {
										if (addLFVAllergensItemsList == "")
											addLFVAllergensItemsList = trimmedItem;
										else
											addLFVAllergensItemsList += "," + trimmedItem;
										allergyNut = true;
									}
								}
							}

							// Determine the type of allergen presence
							if (allergyMilk && allergyNut) {
								System.out.println("LFV Recipe has both Milk and Nut allergens");
								recipes_Allergy_Milk.put("LFV :" + Integer.toString(LFVCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, "Milk" });
								recipes_Allergy_Nut.put("LFV :" + Integer.toString(LFVCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, addLFVAllergensItemsList });

							} else if (allergyMilk) {
								System.out.println("LFV Recipe has Milk allergen only");
								recipes_Allergy_Milk.put("LFV :" + Integer.toString(LFVCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, "Milk" });
							} else if (allergyNut) {
								System.out.println("LFV Recipe has Nut allergen only");
								recipes_Allergy_Nut.put("LFV :" + Integer.toString(LFVCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, addLFVAllergensItemsList });
							} else {
								System.out.println("LFV Recipe has no milk or nut allergens");
							}
							LFVCounter = LFVCounter + 1;
						}

						// ******Iterate LCHF Elimination array list using for loop and compare each
						// value****
						// ****** with Ingredients to filter recipes*****

						boolean validLCHFRecipe = true;
						for (String eliminatedItem : LCHF_EliminateItemList) {

							if (ingredients.contains(eliminatedItem) && eliminatedItem.trim() != "") {
								validLCHFRecipe = false;
								break;
							}
						}

						if (validLCHFRecipe) {

							System.out.println("Valid LCHF Recipe ");

							recipes_LCHF_Elimination.put(Integer.toString(LCHFCounter),
									new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory, ingredients,
											preparation_Time, cooking_Time, tags, no_of_servings, cuisine_category,
											recipe_Description, preparation_method, nutrient_values, recipe_URL, "" });

							// check add on items
							boolean addLCHFItemcheck = false;
							String addLCHFItemsList = "";
							for (String addItem : LCHF_AddItemList) {
								if (ingredients.contains(addItem.trim()) && addItem.trim() != "") {
									if (addLCHFItemsList.trim() == "")
										addLCHFItemsList = addItem;
									else
										addLCHFItemsList += "," + addItem;
									addLCHFItemcheck = true;
								}
							}

							if (addLCHFItemcheck) {
								System.out.println("Recipe contains LCHF Add Items : " + addLCHFItemsList);
								recipes_LCHF_Add.put("LCHF :" + Integer.toString(LCHFCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, addLCHFItemsList });
							}

							// code check LCHF allergens
							boolean allergyMilk = false;
							boolean allergyNut = false;
							String addLCHFAllergensItemsList = "";
							for (String allergyItem : allergiesList) {
								String trimmedItem = allergyItem.trim();
								if (!trimmedItem.isEmpty()
										&& ingredients.toLowerCase().contains(trimmedItem.toLowerCase())) {
									if (trimmedItem.equalsIgnoreCase("milk")) {

										allergyMilk = true;
									} else {
										if (addLCHFAllergensItemsList.trim() == "")
											addLCHFAllergensItemsList = trimmedItem;
										else
											addLCHFAllergensItemsList += "," + trimmedItem;
										allergyNut = true;
									}
								}
							}

							// Determine the type of allergen presence
							if (allergyMilk && allergyNut) {
								System.out.println("LCHF Recipe has both Milk and Nut allergens");
								recipes_LCHF_Allergy_Milk.put("LCHF :" + Integer.toString(LCHFCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, "Milk" });
								recipes_LCHF_Allergy_Nut.put("LCHF :" + Integer.toString(LCHFCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, addLCHFAllergensItemsList });

							} else if (allergyMilk) {
								System.out.println("LCHF Recipe has Milk allergen only");
								recipes_LCHF_Allergy_Milk.put("LCHF :" + Integer.toString(LCHFCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, "Milk" });
							} else if (allergyNut) {
								System.out.println("LCHF Recipe has Nut allergen only");
								recipes_LCHF_Allergy_Nut.put("LCHF :" + Integer.toString(LCHFCounter),
										new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory,
												ingredients, preparation_Time, cooking_Time, tags, no_of_servings,
												cuisine_category, recipe_Description, preparation_method,
												nutrient_values, recipe_URL, addLCHFAllergensItemsList });
							} else {
								System.out.println("LCHF Recipe has no milk or nut allergens");
							}
							LCHFCounter = LCHFCounter + 1;
						}
						// using all_recipesCounter to avoid duplications for current recipe
						recipes_scrapped_treemap.put(recipe_ID+"_"+Integer.toString(all_recipesCounter),
								new Object[] { recipe_ID, recipe_Name, recipe_Category, foodCategory, ingredients,
										preparation_Time, cooking_Time, tags, no_of_servings, cuisine_category,
										recipe_Description, preparation_method, nutrient_values, recipe_URL, "" });
						System.out.println("Display Recipes count : "+recipeUrls.size());
						
							driver.navigate().back();
					}

				} while (clickNext());

			}
			System.out.println("\nTotal number of " + foodCategory + " recipes scrapped are: " + totalRecipes);

		}

		System.out.println("\n********************************************");

		// LOW FAT VEGAN RECIPES
		System.out.println("Total Valid LFV Recipe(After Elimination) = " + recipes_LFV_Elimination.size());
		System.out.println("Total Valid LFV Recipe(On Add) = " + recipes_LFV_Add.size());
		System.out.println("Total LFV Allergy Milk Recipes = " + recipes_Allergy_Milk.size());
		System.out.println("Total LFV Allergy Nut Recipes = " + recipes_Allergy_Nut.size());

		// LOW CARB HIGH FAT RECIPES
		System.out.println("Total Valid LCHF Recipe(After Elimination) = " + recipes_LCHF_Elimination.size());
		System.out.println("Total Valid LCHF Recipe(On Add) = " + recipes_LCHF_Add.size());
		System.out.println("Total LCHF Allergy Milk Recipes = " + recipes_LCHF_Allergy_Milk.size());
		System.out.println("Total LCHF Allergy Nut Recipes = " + recipes_LCHF_Allergy_Nut.size());

		System.out.println("********************************************");

		// insert data to database tables

		dbQuery.insertRow(conn, "lfv_recipes_with_eliminateitems", recipes_LFV_Elimination);
		dbQuery.insertRow(conn, "lfv_recipes_with_addon_items", recipes_LFV_Add);
		dbQuery.insertRow(conn, "lfv_recipes_allergy_with_milk", recipes_Allergy_Milk);
		dbQuery.insertRow(conn, "lfv_recipes_allergy_with_nut", recipes_Allergy_Nut);

		dbQuery.insertRow(conn, "lchf_recipes_with_eliminateitems", recipes_LCHF_Elimination);
		dbQuery.insertRow(conn, "lchf_recipes_with_addon_items", recipes_LCHF_Add);
		dbQuery.insertRow(conn, "lchf_recipes_allergy_with_milk", recipes_LCHF_Allergy_Milk);
		dbQuery.insertRow(conn, "lchf_recipes_allergy_with_nut", recipes_LCHF_Allergy_Nut);

		// All recipes
		dbQuery.insertRow(conn, "recipes_scrapped_by_foodcategory", recipes_scrapped_treemap);

	}

	private void readExcelData() throws Exception {
		read_LFV_Data_Excel();
		read_LCHF_Data_Excel();
		read_FoodCategoryData_Excel();
	}

	private void searchFood(String foodCategory) {
		driver.findElement(By.name("query")).sendKeys(foodCategory);
		WebElement searchButton = driver.findElement(By.cssSelector("button.search-btn"));
		js.executeScript("arguments[0].click();", searchButton);
	}

	private int getRecipeCount() {
		String text = driver.findElement(By.xpath("//p[contains(text(),'results for')]")).getText();
		Matcher matcher = Pattern.compile("\\d+").matcher(text);
		return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
	}

	private boolean clickNext() throws InterruptedException {
		List<WebElement> next = driver.findElements(By.xpath("//a[text()='Next']"));
		if (!next.isEmpty() && next.get(0).isDisplayed()) {
			js.executeScript("arguments[0].click();", next.get(0));
			Thread.sleep(500);
			return true;
		}
		return false;
	}

	public void read_LFV_Data_Excel() {
		ExcelReader reader = new ExcelReader("./src/test/resources/IngredientsAndComorbidities-ScrapperHackathon.xlsx");
		Boolean sheetCheck = reader.isSheetExist("Final list for LFV Elimination ");
		System.out.println("Is the LFV Datasheet exist? -  " + sheetCheck);
		for (int i = 3; i <= 76; i++) {
			String testData = reader.getCellData("Final list for LFV Elimination ", 0, i);
			LFV_EliminateItemList.add(testData.toLowerCase());
		}
		for (int i = 3; i <= 90; i++) {
			String testData = reader.getCellData("Final list for LFV Elimination ", 1, i);
			LFV_AddItemList.add(testData.toLowerCase());
		}
		Boolean allergysheetCheck = reader.isSheetExist("Filter -1 Allergies - Bonus Poi");
		System.out.println("Is the Allergies Datasheet exist ? -  " + allergysheetCheck);
		for (int i = 2; i <= 14; i++) {
			String testData = reader.getCellData("Filter -1 Allergies - Bonus Poi", 0, i);
			allergiesList.add(testData.toLowerCase());
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
