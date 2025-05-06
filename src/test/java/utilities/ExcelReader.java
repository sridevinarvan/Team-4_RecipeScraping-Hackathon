package utilities;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
	public static void readFoodCategoriesFromExcel(String filePath, String ingredients) throws IOException {
		FileInputStream fis = new FileInputStream(filePath);
		Workbook workbook = new XSSFWorkbook(fis);

		// Read from second sheet (index 1)
		Sheet sheet = workbook.getSheetAt(1);

		boolean matchFound = false;
		// Start from row index 2 (third row), to skip headers
		for (int i = 2; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null)
				continue;

			Cell cell = row.getCell(1); // Column B (index 1)
			if (cell == null)
				continue;

			String excelIngredient = cell.getStringCellValue().trim();
			if (excelIngredient.isEmpty())
				continue;

			if (ingredients.toLowerCase().contains(excelIngredient.toLowerCase())) {
				System.out.println("Row " + (i + 1) + " | Ingredient: \"" + excelIngredient + "\" -> Match Found");
				matchFound = true;
			} else {
//				System.out.println("Row " + (i + 1) + " | Ingredient: \"" + excelIngredient + "\" -> Match Not Found");
			}
		}

		workbook.close();
		fis.close();

	}
}
