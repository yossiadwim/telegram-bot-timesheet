package com.example.timesheet;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
public class Timesheet {

    static WebDriver driver;

    @Value("${telegram.url.technoapp}")
    private String url;

    @Value("${telegram.url.technoapp.timesheet}")
    private String urlTimesheet;


    public void login (String email, String password) {
        driver = new ChromeDriver();
        driver.get(url);
        driver.manage().window().maximize();

        WebElement input_email = driver.findElement(By.xpath("//input[@placeholder='cth : mail@mail.com']"));
        input_email.sendKeys(email);

        WebElement input_password = driver.findElement(By.xpath("//input[@id='inputpassword']"));
        input_password.sendKeys(password);

        WebElement button_login = driver.findElement(By.xpath("//button[@id='button-login']"));
        button_login.click();
    }


    public void fillTimesheet(String filePath) throws IOException {

        driver.get(urlTimesheet);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(200));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        FileInputStream file = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet1 = workbook.getSheetAt(0);

        WebElement startDateTimesheet = driver.findElement(By.xpath("//input[@id='start-date-timesheet']"));
        WebElement endDateTimesheet = driver.findElement(By.xpath("//input[@id='end-date-timesheet']"));
        WebElement projectManager = driver.findElement(By.xpath("//select[@id='input-project-manager']"));
        WebElement monthProject = driver.findElement(By.xpath("//select[@id='month_timesheet']"));
        WebElement yearProject = driver.findElement(By.xpath("//select[@id='year_timesheet']"));

        LocalDate startDate = sheet1.getRow(1).getCell(0).getLocalDateTimeCellValue().toLocalDate();
        String formattedStartDate = startDate.format(formatter);
        startDateTimesheet.clear();
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", startDateTimesheet, formattedStartDate);

        LocalDate endDate = sheet1.getRow(1).getCell(1).getLocalDateTimeCellValue().toLocalDate();
        String formattedEndDate = endDate.format(formatter);
        endDateTimesheet.clear();
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", endDateTimesheet, formattedEndDate);

        Select selectProjectManager = new Select(projectManager);
        selectProjectManager.selectByVisibleText(sheet1.getRow(1).getCell(2).getStringCellValue());

        Select selectMonth = new Select(monthProject);
        selectMonth.selectByVisibleText(sheet1.getRow(1).getCell(3).getStringCellValue());

        Select selectYear = new Select(yearProject);
        selectYear.selectByValue(String.valueOf((int) sheet1.getRow(1).getCell(4).getNumericCellValue()));

        // table timesheet
        int rows = sheet1.getLastRowNum();
        for (int i = 4; i < rows; i++) {
            try {
                WebElement buttonAdd = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Add Row']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", buttonAdd);
                buttonAdd.click();
            } catch (ElementClickInterceptedException e) {
                System.out.println("Elemen terhalang, coba lagi dengan JavaScript click.");
                WebElement buttonAdd = driver.findElement(By.xpath("//button[normalize-space()='Add Row']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonAdd);
            }
        }

        List<WebElement> rowsData = driver.findElements(By.xpath("//body[1]/div[1]/div[1]/section[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[3]/div[1]/table[1]/tbody[1]/tr"));
        for (int i = 1; i < rowsData.size()+1; i++) {
            WebElement dateColumn = driver.findElement(By.xpath("//input[@id='date-activity" + i + "']"));
            WebElement activityColumn = driver.findElement(By.xpath("//textarea[@id='activity" + i + "']"));
            WebElement projectNameColumn = driver.findElement(By.xpath("//input[@id='project-name" + i + "']"));
            WebElement activityType = driver.findElement(By.xpath("//select[@id='activity-type" + i + "']"));
            WebElement requestByColumn = driver.findElement(By.xpath("//select[@id='request-by" + i + "']"));
            WebElement targetDateColumn = driver.findElement(By.xpath("//input[@id='target-date" + i + "']"));
            WebElement statusColumn = driver.findElement(By.xpath("//select[@id='status" + i + "']"));
            WebElement timeStartColumn = driver.findElement(By.xpath("//input[@id='time-start" + i + "']"));
            WebElement timeEndColumn = driver.findElement(By.xpath("//input[@id='time-end" + i + "']"));

            //date
            LocalDate date = sheet1.getRow(i + 3).getCell(0).getLocalDateTimeCellValue().toLocalDate();
            String formattedDate = date.format(formatter);
            dateColumn.clear();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", dateColumn, formattedDate);


            //activity
            String activity = sheet1.getRow(i + 3).getCell(1).getStringCellValue();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", activityColumn, activity);
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('input'))", activityColumn);

            //project name
            String projectName = sheet1.getRow(i + 3).getCell(2).getStringCellValue();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", projectNameColumn, projectName);

            //activity type
            Select select = new Select(activityType);
            select.selectByVisibleText(sheet1.getRow(i + 3).getCell(3).getStringCellValue());

            //request by
            Select select2 = new Select(requestByColumn);
            select2.selectByVisibleText(sheet1.getRow(i + 3).getCell(4).getStringCellValue());

            //target date
            LocalDate target = sheet1.getRow(i + 3).getCell(5).getLocalDateTimeCellValue().toLocalDate();
            String formattedTarget = target.format(formatter);
            targetDateColumn.clear();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", targetDateColumn, formattedTarget);

            //status
            Select select3 = new Select(statusColumn);
            select3.selectByValue(sheet1.getRow(i + 3).getCell(6).getStringCellValue());

            //time start
            String timeStart = sheet1.getRow(i + 3).getCell(7).getLocalDateTimeCellValue().toLocalTime().toString();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", timeStartColumn, timeStart);

            //time end
            String timeEnd = sheet1.getRow(i + 3).getCell(8).getLocalDateTimeCellValue().toLocalTime().toString();
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", timeEndColumn, timeEnd);

            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", timeStartColumn);
            ((JavascriptExecutor) driver).executeScript("arguments[0].dispatchEvent(new Event('change'))", timeEndColumn);

        }
    }

}
