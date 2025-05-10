package com.example.timesheet;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TimesheetTest {
    @Test
    void openFile() throws IOException {
        FileInputStream file = new FileInputStream("downloads/timesheet.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet1 = workbook.getSheetAt(0);

        int rows = sheet1.getLastRowNum();

        for (int i = 4; i < rows; i++) {
            System.out.println(sheet1.getRow(i).getCell(3).getStringCellValue());
        }


    }
}