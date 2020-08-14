/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxsoftware.seleniumsandbox;

/**
 *
 * @author Murilo
 */
public class YPBusiness {
    private String name;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String rating;
    private String url;
    private String numReviews;
    
    public YPBusiness(String[] dataRecord, String rating) {
        
        this.name = dataRecord[0].substring(dataRecord[0].indexOf(".") + 2);
        // now in a regular record, data will be in 4, 5 and 6. But if the website view line is missing, then it's 3, 4, 5.
        int dataRow = dataRecord[3].contains("Website") || 
                      dataRecord[3].contains("View") ||
                      dataRecord[3].contains("Menu") ? 4 : 3;
        // now instead of using 4, 5, 6 or 3, 4, 5, we have to use this int and increment it when needed
        this.phone = dataRecord[dataRow++]; //4
        if(dataRecord[5].startsWith("View all")) { // when there's more than one location there's a row "View all X locations"
            dataRow++;
        }
        this.address = dataRecord[dataRow++]; //5 or 6 if there's more than one location
        this.city = dataRecord[dataRow].split(",")[0]; //6 or 7 if there's more than one location
        this.state = dataRecord[dataRow].split(" ")[1]; //6 or 7 if there's more than one location
        this.zip = dataRecord[dataRow].split(" ")[2]; //6 or 7 if there's more than one location
        //this.rating = parseRating(rating);
        this.rating = rating;
        this.url = "http://www.test.com"; //TODO Get the real url
        this.numReviews = dataRecord[2].split("\\)")[0].replace("(","");
        
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        
        if (!(o instanceof YPBusiness)) {
            return false;
        }
        
        YPBusiness compared = (YPBusiness) o;
        
        return (this.name.equals(compared.name) &&
                this.phone.equals(compared.phone) &&
                this.address.equals(compared.address) &&
                this.city.equals(compared.city) &&
                this.state.equals(compared.state) &&
                this.zip.equals(compared.zip) &&
                this.rating.equals(compared.rating) &&
                this.url.equals(compared.url) &&
                this.numReviews.equals(compared.numReviews)
               );
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getRating() {
        return rating;
    }

    public String getUrl() {
        return url;
    }
    
    public String getNumReviews() {
        return numReviews;
    }
    public String parseRating(String rating) {
        String retVal = "";
        switch(rating.split(" ")[0]) {
            case "one":   retVal = "1";
                          break;
            case "two":   retVal = "2";
                          break;
            case "three": retVal = "3";
                          break;
            case "four":  retVal = "4";
                          break;
            case "five":  retVal = "5";
                          break;
            default:      break;              
        }
        if(rating.contains("half"))
            retVal = retVal + ".5";
        return retVal;
    }
}
