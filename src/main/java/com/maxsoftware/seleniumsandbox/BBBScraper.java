/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxsoftware.seleniumsandbox;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Murilo
 */
public class BBBScraper {
    /** XPATHs below were found by using the INSPECT option in Chrome Developer Tools, then right-clicking 
     *  the highlighted text and right-click->Copy->Copy XPath
     *  Source: https://medium.com/@galabramovitz/web-scraping-101-using-selenium-for-java-9d37c52ce7a2
     *  String languagesParagraphXpath = “//*[@id=\"page1\"]/div[2]/div[5]”;
     *  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(languagesParagraphXpath)));
     */
    private final String noResultsXPath = "//*[@id=\"root\"]/div/div/div/main/div/div[2]/div/div[2]/div/h2[1]";
    private final String resultClassXPath = "//*[@id=\"root\"]/div/div/div/main/div/div[2]/div/div[4]/div";
    private final String nameClassElement = ".//a[@class=Name__Link-sc-1srnbh5-1.gNkQmF";
                                            // The above should bring me a list of the elements
                                            
    private WebDriver driver;
    private String what;
    private String where;
    private int pageLimit;
    private ArrayList<BBBBusiness> bBBList;
    
    public BBBScraper(WebDriver driver, String what, String where, int pageLimit) {
        this.driver = driver;
        this.what = what;
        this.where = where;
        this.pageLimit = pageLimit;
        
        // now modify the contents of where to replace special characters
        this.where = this.where.replace(",", "%2C").replace(" ", "%20");
        
        this.bBBList = new ArrayList<>();
    }
    
    public void scrapeBBB() {
        final String accredited = "1"; // right now, fixed in accredited for ratings
        int page = 1;
        final String resultNumXPath = "//*[@id=\"root\"]/div/div/div/main/div[2]/div[2]/div/div[1]/h2/strong[1]";
        driver.get("https://www.bbb.org/search?filter_accredited=" + accredited + 
                       "&find_country=USA&find_loc=" + where + 
                       "&find_text=" + what + 
                       "&page=" + page );
        //TODO work pages requested OR All
        if(driver.findElements(By.xpath(noResultsXPath)).isEmpty() == true) {
            this.waitForPageLoaded();
            if(driver.findElements(By.id("radioBBB")).size() > 0) {
                driver.findElement(By.id("radioBBB")).click(); // get rid of first result page popup option
            }
            
            // Thisis where I first got the list results. Moving to inside the loop -> List<WebElement> resultList = driver.findElements(By.xpath(resultClassXPath));
            //DEBUGSystem.out.println("Number of results:" + driver.findElements(By.xpath("//*[@id=\"root\"]/div/div/div/main/div/div[2]/div/div[1]/h2[1]/strong[1]")).get(0).getText());
            String resultNumStr = driver.findElements(By.xpath("//*[@id=\"root\"]/div/div/div/main/div/div[2]/div/div[1]/h2[1]/strong[1]")).get(0).getText();
            
            if(resultNumStr != null) {
                int resultNum = Integer.valueOf(resultNumStr);
                do // do it as least once
                {
                    if(page > 1) { // for the first page there's one already in place.
                        this.waitForPageLoaded();
                    }
                    List<WebElement> resultList = driver.findElements(By.xpath(resultClassXPath));
                    System.out.println("Size of result number list:" + resultList.size());
                    for (WebElement item : resultList) {
                        System.out.println("\n" + item.getText());
                        String myString[] = item.getText().split("\\r?\\n"); // split by line break. This covers both windows and linux
                        addBBBRecord(myString);
                        //System.out.println("Name: " + item.findElement(By.xpath( "//*[@id=\"root\"]/div/div/div/main/div/div[2]/div/div[4]/div[5]/div/div/div[2]/div[1]/h3/a")).getText());

                    }
                    System.out.println("Finished adding page results for page" + page + ", results:" + bBBList.size());
                    // get Next Page
                    driver.get("https://www.bbb.org/search?filter_accredited=" + accredited + 
                                   "&find_country=USA&find_loc=" + where + 
                                   "&find_text=" + what + 
                                   "&page=" + ++page );
                } while (   (pageLimit == 0 && ( (page * 15) <= resultNum)) // do it if getting all and page hasn't reached the limit yet
                         || (pageLimit != 0 && page <= pageLimit) ); // or just until it reaches end page
            }
            

        }
                
        
    }
    
    /**
     * Source: https://devqa.io/webdriver-wait-page-load-example-java/
     */
    public void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
                    }
                };
        try {
            Thread.sleep(1000);
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(expectation);
        } catch (Throwable error) {
            System.out.println("Timeout waiting for Page Load Request to complete.");
        }
    }
    
    public ArrayList<BBBBusiness> getBBBResults() {
        return bBBList;
    }
    
    public void addBBBRecord(String[] businessData) {
        if (businessData.length >= 7) { // if less, it's not rated or not enough data
            BBBBusiness oBBBBusiness = new BBBBusiness(businessData);
            if(!bBBList.contains(oBBBBusiness)) {
                bBBList.add(oBBBBusiness);
            }
        }
        
    }
}
