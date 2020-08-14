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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Murilo
 */
public class YPScraper {
    private WebDriver driver;
    private String what;
    private String where;
    private int pageLimit;
    private ArrayList<YPBusiness> yPList;
    
    public YPScraper(WebDriver driver, String what, String where, int pageLimit) {
        this.driver = driver;
        this.what = what;
        this.where = where;
        this.pageLimit = pageLimit;
        this.where = this.where.replace(",", "%2C").replace(" ", "");
        this.yPList = new ArrayList<>();
    }
    
    public ArrayList<YPBusiness> getYPResults() {
        return yPList;
    }
    
    public void addYPRecord(String[] businessData, String rating) {
        if (businessData.length >= 7 && businessData[2].startsWith("(")) { // if there's a rating, then the third line contains the number of reviews. If it doesn't, then it changes all the placements
            YPBusiness oYPBusiness = new YPBusiness(businessData, rating); // rating not in the text
            if(!yPList.contains(oYPBusiness)) {
                yPList.add(oYPBusiness);
            }
        }
        
    }
    
    public void scrapeYP() {
        driver.get("https://www.yellowpages.com/search?search_terms=" + this.what + 
                   "&geo_location_terms=" + this.where );
        // TODO check if no results are found, else...
        String resultNumStr = driver.findElements(By.xpath("//*[@id=\"main-content\"]/div[2]/div[4]/p")).get(0).getText();
        if(resultNumStr != null) {
            int resultNum = Integer.valueOf(resultNumStr.replace("We found","").replace("results", ""));
            int page = 1;
            System.out.println("Beginning YP Scrape...");
            do // do it as least once
            {   
                if(page > 1) { // for the first page there's one already in place.
                        this.waitForPageLoaded();
                }
                List<WebElement> resultList = driver.findElements(By.className("result"));
                System.out.println("Size of result number list:" + resultList.size());
                for (WebElement item : resultList) {
                    System.out.println("\n" + item.getText());
                    String myString[] = item.getText().split("\\r?\\n"); // split by line break. This covers both windows and linux
                    //WAS:String rating = item.findElement(By.xpath("//div[starts-with(@class, 'result-rating')]")).getAttribute("class");
                    //WAS:rating = rating.replace("result-rating ","");
                    String rating = getRating(item.getAttribute("innerHTML"));
                    /*
                    if(!rating.equals("not rated")) {
                        String numReviews = getNumReviews(item.getAttribute("innerHTML"));
                    }
                    */
                    addYPRecord(myString, rating); // rating passed separately because it doesn't exist in the text
                }
                System.out.println("Finished adding page results for page" + page + ", results:" + yPList.size());
                // get Next Page
                driver.get("https://www.yellowpages.com/search?search_terms=" + this.what + 
                           "&geo_location_terms=" + this.where + 
                           "&page=" + ++page );
                System.out.println("End of loop");
            } while (   (pageLimit == 0 && ( (page * 30) <= resultNum)) // do it if getting all and page hasn't reached the limit yet - 30 Results per page
                     || (pageLimit != 0 && page <= pageLimit) ); // or just until it reaches end page
        }
        
    }
    
    public String getRating(String YPItem) {
        //This shows what I want, or nothing:
        //System.out.println(item.getAttribute("innerHTML").substring(item.getAttribute("innerHTML").indexOf("result-rating ")).split(" ")[1])
                String retVal = "not rated";
                if((YPItem.contains("result-rating ")) &&
                   (YPItem.substring(YPItem.indexOf("result-rating ")).split(" ").length > 1)) { // fetch result-rating node, split the rest of the string in an array
                    retVal = YPItem.substring(YPItem.indexOf("result-rating ")).split(" ")[1];
                    // This variable adds a .5 after the rating is converted
                    boolean half = ((YPItem.substring(YPItem.indexOf("result-rating ")).split(" ").length > 2) && 
                                    (YPItem.substring(YPItem.indexOf("result-rating ")).split(" ")[2].equals("half")));
                        
                    switch(retVal) {
                        case "one": retVal = "1";
                                    break;
                        case "two": retVal = "2";
                                    break;
                        case "three": retVal = "3";
                                      break;
                        case "four": retVal = "4";
                                     break;
                        case "five": retVal = "5";
                                     break;
                        default: retVal = "None";
                    }
                    if(half) {
                        retVal = retVal + ".5";
                    }
                }
                return retVal;
    }
    
    public String getNumReviews(String YPItem) {
        //This shows what I want, or nothing:
        //System.out.println(item.getAttribute("innerHTML").substring(item.getAttribute("innerHTML").indexOf("result-rating ")).split(" ")[1])
                String retVal = "not rated";
                if((YPItem.contains("<span class=\"count\">")) &&
                   (YPItem.substring(YPItem.indexOf("<span class=\"count\">")).split("(").length > 1)) { // fetch count node, split the rest of the string in an array
                    retVal = YPItem.substring(YPItem.indexOf("<span class=\"count\">")).split("(")[1].split(")")[0];
                }
                return retVal;
    }
    
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
}
