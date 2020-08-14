/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxsoftware.seleniumsandbox;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
/**
 * YELP API imports
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.json.*;
/**
 * /YELP API
 */

/**
 * EXCEL imports
 */
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *
 * @author Murilo
 */
public class WebTools {
    private WebDriver driver;
    private JSONArray yelpResponse; // used to hold Yelp Fusion API responses
    private ArrayList<YelpBusiness> yelpData;
    private ArrayList<BBBBusiness> bBBData;
    private ArrayList<YPBusiness> yPData;
    
    //Excel variables
    private final String excelDataSource = "Indicadores_USA.xlsx";
    private Sheet datatypeSheet;
    private Sheet exportSheet;
    private Workbook workbook;
    private FileInputStream excelFile;
    private int iRow;
    
    public WebTools() throws Exception {
        yelpData = new ArrayList<>();
        bBBData = new ArrayList<>();
        yPData = new ArrayList<>();
        System.setProperty("webdriver.chrome.driver", "c:\\Java-Selenium\\Chromedriver\\chromedriver.exe");
        try {
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            driver.manage().deleteAllCookies();
            driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        } catch(Exception e) {
                System.out.println("Error instantiating ChromeDriver: " + e.getMessage());
                // Troubleshooting: From time to time, it's necessary to download and paste the new chromedriver over the existing.
        }
    }
    
    public String getUnemploymentRateViaWeb(String searchTerms) {
        String returnValue = "Error";
        try {
            driver.get("https://www.google.com/search?q=" + searchTerms.replace(" ", "+"));
            //This is not working because results changed<< returnValue = driver.findElement(By.xpath("//*[contains(@class,'ayqGOc')]")).getText();
            returnValue = driver.findElement(By.xpath("//*[contains(@data-tts,'answers')]")).getText();
            // Other attempts:
            //div[@id='alertLabel' 
            //WAS ->driver.findElement(By.xpath("//input[@id='login-username']")).sendKeys("edureka@yahoo.com");
        } catch (Exception e) {
            System.out.println("Error in getUnemploymentRate:" + e.getMessage());
        }
        return returnValue;
    }
    
    public String getUnemploymentRate() {
        //getUnemploymentRateViaWeb(searchTerms); <-- Would need to pass searchTerms to this routine
        return getUnemploymentRateFromSpreadsheet();
    }
    
    public ObservableList<BBBBusiness> getBBBRatings(String searchTerms, String searchLocation, int numQueries) {
        BBBScraper bbbS = new BBBScraper(driver, searchTerms, searchLocation, numQueries);
        bbbS.scrapeBBB();
        System.out.println("Size of result list: " + bbbS.getBBBResults().size());
        bBBData = bbbS.getBBBResults();
        return FXCollections.observableArrayList(bBBData);
    }
    
    public ObservableList<YPBusiness> getYPRatings(String searchTerms, String searchLocation, int numQueries) {
        YPScraper yPS = new YPScraper(driver, searchTerms, searchLocation, numQueries);
        yPS.scrapeYP();
        yPData = yPS.getYPResults();
        return FXCollections.observableArrayList(yPData);
        
    }
    
    public String getYelpRatings(String searchTerms, String searchLocation, int numQueries) throws Exception, IOException, JSONException {
        // Main page for the Yelp Fusion API: https://github.com/Yelp/yelp-fusion/pull/171
        // FROM: SAMPLE @ https://github.com/Yelp/yelp-fusion/blob/0102992a0ab77ab6b529fbff50fc0de570c7a634/fusion/java/sample.java
        // POST https://api.yelp.com/oauth2/token
        
        String accessToken=null;
        try {
            // GET /businesses/search
            JSONObject jsonObject;
            OkHttpClient client2 = new OkHttpClient();
            int numCalls = 0;
            Integer total = 0;
            System.out.println("getting into while");
            while(true) {
                numCalls++;
                System.out.println("Now making call number " + numCalls);
                Request request = this.buildYelpRequest(searchTerms, searchLocation);
                Response response2 = client2.newCall(request).execute();
                jsonObject = new JSONObject(response2.body().string().trim()); // parser
                if(jsonObject.has("total")) {
                    if(total == 0) {
                        total = (Integer) jsonObject.get("total");
                    }
                    JSONArray myResponse = (JSONArray)jsonObject.get("businesses");
                    if (this.yelpResponse == null) {
                        this.yelpResponse = myResponse;
                    } else {
                        for (int i = 0; i < myResponse.length(); i++) {
                            this.yelpResponse.put(myResponse.getJSONObject(i));
                        }
                    }
                }
                
                if( ( (numCalls * 20) >= total) || (numQueries != 0 && numCalls == numQueries) ) {
                    break;
                }
            }
            
            /**
             * by BU: So the JSONObject above has 3 Indices in it: businesses, with the list of 
             * businesses on Yelp, total with the total records returned and region, with coordinates 
             * to the location you selected. At this point, the total and businesses are the only ones that actually matter.
             **/
            
            //This prints the id for the first result: System.out.println("MuriloPrint: " + myResponse.getJSONObject(0).getString("id"));
            
            /* This prints the JSON, DEBUG
            for (int i = 0; i < myResponse.length(); i++) {
                System.out.println(myResponse.getJSONObject(i).toString());
            }
            */
            
            /* Result is total, region, businesses
            Iterator myIterator = jsonObject.keys();
            while (myIterator.hasNext()) {
                System.out.println(myIterator.next().toString());
            }
            */
            //Integer total = (Integer) jsonObject.get("total");
            System.out.println("Murilo-Result Of Total:" + total);
            /* This prints the region (basically the place you're querying, or your location). We don't need this
            JSONObject regionResponse = (JSONObject)jsonObject.get("region");
            System.out.println(regionResponse.toString());
            */

        } catch (IOException e) {
            e.printStackTrace();
        } catch(JSONException jsonEx) {
            System.out.println("Found JSONException:" + jsonEx.getMessage());
            jsonEx.printStackTrace();
        }
        return "result";
    }
       
    /**
     * 
     * @return String with the headers to be used by the Controller to add them to the TableView
     */
    public String getYelpHeaders() {
        return "Name,Rating,Review Count,Transactions,Display Phone,Price,Closed?,URL";
    }
    
    public String getYelpFields() {
        return "name,rating,reviewCount,transactions,displayPhone,price,closed,url";
    }
    
    public String getBBBFields() {
        return "name,rating,phone,address,city,state,zip";
    }
    
    public String getYPFields() {
        return "name,rating,numReviews,phone,address,city,state,zip,url";
    }
    
    
    public ObservableList<YelpBusiness> getYelpResults() {
        try {
            for (int i = 0; i < yelpResponse.length(); i++) {
                String name = getStringNode(i, "name"); 
                double rating = yelpResponse.getJSONObject(i).getDouble("rating");
                int reviewCount = yelpResponse.getJSONObject(i).getInt("review_count");
                String url = getStringNode(i, "url"); 
                String transactions = String.valueOf(yelpResponse.getJSONObject(i).get("transactions"));
                String displayPhone = getStringNode(i, "display_phone");
                String price = getStringNode(i, "price");
                boolean isClosed = yelpResponse.getJSONObject(i).getBoolean("is_closed");
                //DEBUGSystem.out.println("Retrieved fields. Values" + name + "\n" + rating + "\n" + reviewCount + "\n" + url + "\n" + transactions + "\n" + displayPhone + "\n" + price + "\n" + isClosed);
                YelpBusiness business = new YelpBusiness(name, rating, reviewCount, url, transactions, displayPhone, price, isClosed);
                //DEBUGSystem.out.println("Business object created." + business.toString() + "\n" + business.getName());
                if(!yelpData.contains(business)) {
                    boolean add = yelpData.add(business);
                }
                
                //DEBUGSystem.out.println("Added?" + add);
                //System.out.println(yelpResponse.getJSONObject(i).toString());
            }
        } catch (JSONException jsonEx) {
            System.out.println("JSON Exception: " + jsonEx.getMessage());
        } catch (Exception e) {
            System.out.println("Exception found when retrieving businesses: " + e.getMessage() + " cause: " + e.getCause() + "\nGetClass:" + e.getClass() + "\nGet" + "\nCanonicalName: " + e.getClass().getCanonicalName() + "\nType Name: " + e.getClass().getTypeName());
        }
        System.out.println("MuriloPrint-Number of businesses added: " + yelpData.size());
        return FXCollections.observableArrayList(yelpData);
    }
    
    public void initDriver() {
        this.driver = new ChromeDriver();
    }
    
    public String getStringNode (int i, String field) {
        String retVal = "ERROR";
        try { 
            retVal = yelpResponse.getJSONObject(i).has(field) ? yelpResponse.getJSONObject(i).getString(field) : null;
        } catch(JSONException jsonEx) {
            System.out.println("Error in getStringNode: " + jsonEx.getMessage());
        }
        return retVal;
    }
    
    public void closeDriver() {
        driver.close();
    }
    
    public Request buildYelpRequest(String searchTerms, String searchLocation) {
        
        String term = searchTerms;         // term, i.e "brewery"
        String location = searchLocation;  // location, i.e "Klamath Falls, OR"
        String price = "NOT IMPLEMENTED";  // price, i.e "1" - 1 = $, 2 = $$, 3 = $$$, 4 = $$$$
        Request request2 = new Builder()
                //.url("https://api.yelp.com/v3/businesses/search?term=" + term + "&location=" + location + "&limit=1&sort_by=rating&price="+price+"")
                .url("https://api.yelp.com/v3/businesses/search?term=" + term + "&location=" + location + "&offset=" + ( (yelpResponse == null) ? 0 : yelpResponse.length() ) + "&limit=20&sort_by=rating")
                .get()
                //This apparently changed too. Link below: .addHeader("authorization", "Bearer"+" "+accessToken)
                //https://github.com/Yelp/yelp-fusion/issues/335
                .addHeader("authorization", "Bearer "+"yEO1St2dXPl18XezuaJjHlIAA1XyfcA6A7FAguqs9k9nn2pLhfc0tEpiedIq16Z-Fx8-QvbGoyWxyIZGit-llhPV85uW5U0UhpLmo_arJPfpALUWqrNnBx6q07yyXnYx")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "b5fc33ce-3dad-86d7-6e2e-d67e14e8071b")
                .build();
        return request2;
            
    }
    
    public void startExcel(String dataSource) {
        String templateFileName = dataSource;
        try {
            excelFile = new FileInputStream(templateFileName);
            this.workbook = WorkbookFactory.create(excelFile);
            // We're just reading, so I'm streamlining the commands
            
            // This closes excel -> excelFile.close();
            //outputStream.close();            
        } catch (FileNotFoundException e) {
            System.out.println("1 " + e.getMessage());
        } catch (IOException e) {
            System.out.println("2 " + e.getMessage());
        } catch (Exception e) {
            System.out.println("3 " + e.toString() + " - " + e.getLocalizedMessage() + " - " + e.getMessage());
        }    
    }
    
    public String getUnemploymentRateFromSpreadsheet() {
        String retVal = "Not found";
        if (excelFile == null) {
            System.out.println("Starting Excel");
            startExcel(excelDataSource);
        }
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        this.datatypeSheet = this.workbook.getSheetAt(0);
        retVal = this.datatypeSheet.getRow(year - 2009).getCell(month).toString(); // Because 2010 = 1 (row 2, but Java starts at zero)
        return retVal != null ? retVal : "Error fetching unemployment";
    }
    
    public String getConsumerConfidence() {
        String retVal = "Not found";
        if (excelFile == null) {
            startExcel(excelDataSource);
        }
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        String formedDate = year + "-" + (month < 10? "0" : "") + month;
        Iterator<Row> rowIterator = datatypeSheet.rowIterator();
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(row.getCell(4).toString().equals(formedDate)) { // TIME column. Currently, number 5, but 4 in Java
              retVal = row.getCell(5).toString();
            }
        }
        return retVal;
    }
            
    /**
     * exportToExcel - Exports the fetched data to Excel.
     * @throws Exception 
     */
    public void exportToExcel() throws Exception {
        this.iRow = 0;
        FileInputStream exportExcelFile = new FileInputStream("SMB1template.xlsx");
        Workbook exportWorkbook = WorkbookFactory.create(exportExcelFile);
        exportWorkbook.setSheetName(1, "BusinessRawData");
        exportSheet = exportWorkbook.getSheetAt(1);
        String columns[] = {"Name","Phone","Address","City","State","Zip","Rating","# Reviews","URL","Delivery","Pickup","Closed"};
        for(int i = 0; i < columns.length; i++) {
            fillCell(i, columns[i]);
        }
        this.iRow++;
        
        this.processList(this.yelpData);
        this.processList(this.bBBData);
        this.processList(this.yPData);
        String tempDir = this.getTmpDir();
        String absoluteFileName = tempDir + "SMB1template.xlsx";
        FileOutputStream outputStream = new FileOutputStream(absoluteFileName);
        System.out.println("Saved as " + absoluteFileName);
        exportWorkbook.write(outputStream);
        exportExcelFile.close();
        outputStream.close();            
        //open Excel
        Desktop.getDesktop().open(new File(absoluteFileName));
        
        //throw new Exception ("NOT IMPLEMENTED");
    }   
    
    /**
     * getTmpDir - retrieves Java's temp directory
     * @return a String with the property content.
     */
    public String getTmpDir() {
        // This is the property name for accessing OS temporary directory
        // or folder.
        String property = "java.io.tmpdir";

        // Get the temporary directory and return it.
        return System.getProperty(property);
    }
    
    /**
     * fillCell - fills a determined Excel spreadsheet cell in the sheet supplied, at the position (row,col) with the provided text.
     * @param iCol
     * @param text 
     */
    private void fillCell(int iCol, String text) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if(text.equals("0")) return;
        
        Row row = getNewRow(this.exportSheet, this.iRow);
        Cell cell = getNewCell(row, iCol);
        if(isNumeric(text)) {
            Workbook workbook = exportSheet.getWorkbook();
            CellStyle cs = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            cs.setDataFormat(df.getFormat("0"));
            /* border formats - Keep?
            cs.setBorderBottom(cell.getCellStyle().getBorderBottom()); 
            cs.setBorderLeft(cell.getCellStyle().getBorderLeft()); 
            cs.setBorderTop(cell.getCellStyle().getBorderTop()); 
            cs.setBorderRight(cell.getCellStyle().getBorderRight()); 
            */
            cell.setCellValue(Double.parseDouble(text)); 
            cell.setCellStyle(cs);
        } else {
            cell.setCellValue(text); 
        }
    }
    
    /**
     * isNumeric - Tests whether the text is a number.
     * @param text
     * @return boolean - true if it's a number. False otherwise.
     */
    private boolean isNumeric(String text) {
        try {  
            //System.out.println("testing..." + text);
            Integer.parseInt(text);  
            //System.out.println("True");
            return true;
        } catch(NumberFormatException e){  
            //System.out.println("False");
            return false;  
        }  
    }
    
    private Row getNewRow(Sheet datatypeSheet, int iRow) {
        Row row = datatypeSheet.getRow(iRow);

        return (row == null) ? datatypeSheet.createRow(iRow) : row;
    }

    /**
     * getNewCell - Method to get a cell object
     * @param row 0 is 1, Excel Row to get
     * @param iCol 0 is 1, Excel Col to get
     * @return Cell - a new cell if not existing. The cell itself if it does exist.
     */
    private Cell getNewCell(Row row, int iCol) {
        Cell cell = row.getCell(iCol);
        
        return (cell == null) ? row.createCell(iCol) : cell;
    }
    
    private void processList(ArrayList list) {
        for(Object object : list) {
            if(object instanceof YelpBusiness) {
                YelpBusiness yelpB = (YelpBusiness) object;
                
                fillCell( 0, yelpB.getName());
                fillCell( 1, yelpB.getDisplayPhone());
                fillCell( 6, String.valueOf(yelpB.getRating()));
                fillCell( 7, String.valueOf(yelpB.getReviewCount()));
                fillCell( 8, yelpB.getUrl());
                fillCell( 9, yelpB.getDelivery());
                fillCell(10, yelpB.getPickup());
            } else if(object instanceof BBBBusiness) {
                BBBBusiness bBBB = (BBBBusiness) object;
                fillCell( 0, bBBB.getName());
                fillCell( 1, bBBB.getPhone());
                fillCell( 2, bBBB.getAddress());
                fillCell( 3, bBBB.getCity());
                fillCell( 4, bBBB.getState());
                fillCell( 5, bBBB.getZip());
                fillCell( 6, bBBB.getRating());
            } else if(object instanceof YPBusiness) {
                YPBusiness yPB = (YPBusiness) object;
                fillCell( 0, yPB.getName());
                fillCell( 1, yPB.getPhone());
                fillCell( 2, yPB.getAddress());
                fillCell( 3, yPB.getCity());
                fillCell( 4, yPB.getState());
                fillCell( 5, yPB.getZip());
                fillCell( 6, yPB.getRating());
                fillCell( 7, yPB.getNumReviews());
                fillCell( 8, yPB.getUrl());
            } else {
                System.out.println("None of the objects. Bypassing...");
            }
            this.iRow++;
        } // end loop object in list
        
    }
    
    public void destroy() {
        try {
            this.driver.quit();
            
            //this.driver.close();
        } catch (Exception e) {
            System.out.println("Error closing instances:" + e.getMessage());
        }
        
    }

    public void updateSpreadsheet() {
        try {
            Desktop.getDesktop().open(new File(this.excelDataSource));
        } catch (Exception e) {
            System.out.println("Error opening Excel. Error: " + e.getMessage());
        }
        
        
    }
    
    public void clearCurrentResults() {
        this.yelpData.clear();
        this.yelpResponse = null;
        this.bBBData.clear();
        this.yPData.clear();
    }
}
