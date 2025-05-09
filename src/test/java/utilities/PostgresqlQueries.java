package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

public class PostgresqlQueries {

	public Connection createTables_list() throws SQLException {

		Connection conn = PostgresqlDBConnection.getConnection();

		// ALL RESCIPES BASED ON FOOD CATEGORY
		createTable(conn, "recipes_scrapped_by_foodcategory");

		// LOW FAT VEGAN RECIPES
		createTable(conn, "lfv_recipes_with_eliminateitems");
		createTable(conn, "lfv_recipes_with_addon_items");

		// LOW CARB HIGH FAT RECIPES
		createTable(conn, "lchf_recipes_with_eliminateitems");
		createTable(conn, "lchf_recipes_with_addon_items");

		
		// ALLERGY RECIPES
		createTable(conn, "lfv_recipes_allergy_with_milk");
		createTable(conn, "lfv_recipes_allergy_with_nut");
		
		createTable(conn, "lchf_recipes_allergy_with_milk");
		createTable(conn, "lchf_recipes_allergy_with_nut");

		
		System.out.println("Required Tables Created");
		return conn;
	}

	public void createTable(Connection conn, String tableName) {
		Statement statement;
		try {

			// ALL TABLES HAVE SAME COLUMNS
			String query = "create table IF NOT EXISTS " + tableName
					+ "(Recipe_ID varchar(200) PRIMARY KEY, Recipe_Name varchar(2000), Recipe_Category varchar(2000),"
					+ " Food_Category varchar(2000), Ingredients varchar(2000), "
					+ "Preparation_Time varchar(200), Cooking_Time varchar(200), "
					+ "Tag varchar(2000), No_Of_Servings varchar(2000), Cuisine_Category varchar(2000),"
					+ " Recipe_Description varchar(5000), Preparation_Method varchar(5000), "
					+ "Nutrient_Values varchar(2000), Recipe_URL varchar(2000), Ingredients_contains varchar(2000));";
			statement = conn.createStatement();
			statement.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void insertRow(Connection conn, String tableName, Map<String, Object[]> recipe) {

		PreparedStatement preparedStatement = null;
		try {
			Set<String> keyid = recipe.keySet();

			for (String key : keyid) {

				int rowid = 0;

				// Load data from recipe by key
				Object[] recipeObject = recipe.get(key);

				// SQL query with parameterized placeholders
				String query = "INSERT INTO " + tableName
						+ "(Recipe_ID,Recipe_Name,Recipe_Category,Food_Category,Ingredients,Preparation_Time,"
						+ "Cooking_Time,Tag,No_Of_Servings,Cuisine_Category,Recipe_Description, Preparation_Method,"
						+ "Nutrient_Values,Recipe_URL,Ingredients_contains) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				// PreparedStatement object with the SQL query
				preparedStatement = conn.prepareStatement(query);

				// Set values for each parameter in the prepared statement
				preparedStatement.setString(1, (String) recipeObject[0]);// recipe_id"
				preparedStatement.setString(2, (String) recipeObject[1]);// recipe_Name
				preparedStatement.setString(3, (String) recipeObject[2]);// recipe_Category
				preparedStatement.setString(4, (String) recipeObject[3]);// food_Category
				preparedStatement.setString(5, (String) recipeObject[4]);// ingredients
				preparedStatement.setString(6, (String) recipeObject[5]);// preparation_Time
				preparedStatement.setString(7, (String) recipeObject[6]);// cooking_Time
				preparedStatement.setString(8, (String) recipeObject[7]);// tags
				preparedStatement.setString(9, (String) recipeObject[8]);// no_of_servings
				preparedStatement.setString(10, (String) recipeObject[9]);// cuisine_category
				preparedStatement.setString(11, (String) recipeObject[10]);// recipe_Description
				preparedStatement.setString(12, (String) recipeObject[11]);// preparation_method
				preparedStatement.setString(13, (String) recipeObject[12]);// nutrient_values
				preparedStatement.setString(14, (String) recipeObject[13]);// recipe_URL
				preparedStatement.setString(15, (String) recipeObject[14]);// AddTo Ingredient

				// Execute the insert operation
				preparedStatement.executeUpdate();

			}
			System.out.println("Recipes details inserted into :" + tableName);
		} catch (SQLException e) {
			System.out.println("Error inserting row: " + e.getMessage());
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public void closeConnection(Connection conn) {
		PostgresqlDBConnection.closeConnection(conn);
	}
}