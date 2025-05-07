package testCases;

import java.sql.Connection;

import org.testng.annotations.Test;

import base.TestBase;
import pages.TarladalalSearch;
import utilities.PostgresqlQueries;

public class TarlaDalalTest extends TestBase {

	Connection conn = null;

	@Test
	public void scrapeRecipes() throws Exception {
		
		//Opens db connection and creates tables 
		PostgresqlQueries dbQuries = new PostgresqlQueries();
		conn = dbQuries.createTables_list();
		
		//scrape recipes and insert into tables
		TarladalalSearch searchscrape = new TarladalalSearch(driver.get(), conn,dbQuries);
		searchscrape.scrapeAllRecipes();
		
		//close db connection
		dbQuries.closeConnection(conn);		
	}
}
