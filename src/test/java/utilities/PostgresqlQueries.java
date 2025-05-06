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
		createTable(conn, "recipes_scrapped_by_foodcategory");		
		return conn;
	}

	public void createTable(Connection conn, String tableName) {
		Statement statement;
		try {
			String query = "create table IF NOT EXISTS " + tableName
					+ "(Recipe_ID varchar(200) PRIMARY KEY, Recipe_Name varchar(2000), Recipe_Category varchar(2000),"
					+ " Food_Category varchar(2000), Ingredients varchar(2000), "
					+ "Preparation_Time varchar(200), Cooking_Time varchar(200), "
					+ "Tag varchar(2000), No_Of_Servings varchar(2000), Cuisine_Category varchar(2000),"
					+ " Recipe_Description varchar(5000), Preparation_Method varchar(5000), "
					+ "Nutrient_Values varchar(2000), Recipe_URL varchar(2000), AddToIngredient varchar(2000));";
			statement = conn.createStatement();
			statement.executeUpdate(query);
			System.out.println("Table Created");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void insertRow(Connection conn, String tableName, Map<String, Object[]> recipe) {
		
		System.out.println("Inserting into Data base");
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
						+ "Nutrient_Values,Recipe_URL,AddToIngredient) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				// PreparedStatement object with the SQL query
				preparedStatement = conn.prepareStatement(query);
				// Set values for each parameter in the prepared statement
				preparedStatement.setString(1, (String) recipeObject[0]);// recipe_id"
				preparedStatement.setString(2, (String) recipeObject[1]);// recipeName
				preparedStatement.setString(3, (String) recipeObject[2]);// rec_Category
				preparedStatement.setString(4, (String) recipeObject[3]);// food_Category
				preparedStatement.setString(5, (String) recipeObject[4]);// ingredient_List
				preparedStatement.setString(6, (String) recipeObject[5]);// prepTime
				preparedStatement.setString(7, (String) recipeObject[6]);// cookTime
				preparedStatement.setString(8, (String) recipeObject[7]);// tags
				preparedStatement.setString(9, (String) recipeObject[8]);// noOfServings
				preparedStatement.setString(10, (String) recipeObject[9]);// cuisineCategory
				preparedStatement.setString(11, (String) recipeObject[10]);// desc
				preparedStatement.setString(12, (String) recipeObject[11]);// method
				preparedStatement.setString(13, (String) recipeObject[12]);// nutritionValue
				preparedStatement.setString(14, (String) recipeObject[13]);// recipeURL
				preparedStatement.setString(15, (String) recipeObject[14]);// AddToIngredient

				// ecipes_LFV_Elimination.put( Integer.toString(LFVCounter) , new Object[] {

				// Execute the insert operation
				preparedStatement.executeUpdate();

				System.out.println("Row Inserted");
			}
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
	
	public void closeConnection(Connection conn)
	{
		PostgresqlDBConnection.closeConnection(conn);
	}
}