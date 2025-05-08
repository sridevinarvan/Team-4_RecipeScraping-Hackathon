package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostgresqlDBConnection {

	private static final String URL = "jdbc:postgresql://localhost:5431/Team4_RecipeRaiders_May25_db";
	private static final String USER = "postgres";
	private static final String PASSWORD = "admin";

	public static Connection getConnection() throws SQLException {

		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			
			connection = DriverManager.getConnection(URL, USER, PASSWORD);
			
			if (connection != null) {
				System.out.println("\n***Postgresql database connection established***");
			} else {
				System.out.println("\nConnection failed");
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return connection;
	}
	
	public static void closeConnection(Connection connection) {
	    try {
	        if (connection != null && !connection.isClosed()) {
	            connection.close();
	            System.out.println("\n***Connection closed.***");
	        }
	    } catch (SQLException e) {
	        System.out.println("Error while closing connection: " + e.getMessage());
	    }
	}
}
