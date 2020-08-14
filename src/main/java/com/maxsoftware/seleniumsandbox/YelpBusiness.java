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
public class YelpBusiness {
    private String name;
    private double rating;
    private int reviewCount;
    private String url;
    private String transactions;
    private String delivery;
    private String pickup;
    private String displayPhone;
    private String price;
    
    // see how to address Location, which seems to be a JSON inside a JSON
    /**
     * Fields:
     * country, address3, address2, city, address1, display_address (seems to be an array), state, zip_code
     */
    // Categories is another JSON inside a JSON
    /**
     * Fields:
     * alias, title
     */
    private boolean isClosed;
    private String closed;
    
    /**
     * Constructor: YelpBusiness
     * @param name - String
     * @param rating - double
     * @param reviewCount - integer
     * @param url - String
     * @param transactions - String
     * @param displayPhone - String
     * @param price - String
     * @param isClosed - boolean
     */
    public YelpBusiness(String name, double rating, int reviewCount, String url, String transactions, String displayPhone, String price, boolean isClosed) {
        this.name = name != null ? name : "Null";
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.url = url != null ? url : "None";
        this.transactions = transactions;
        this.delivery = transactions.contains("delivery") ? "X" : "";
        this.pickup = transactions.contains("pickup") ? "X" : "";
        this.displayPhone = displayPhone != null ? displayPhone : "";
        this.price = price != null ? price : "?";
        this.isClosed = isClosed;
        this.closed = isClosed? "X" : "";
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public String getUrl() {
        return url;
    }

    public String getTransactions() {
        return transactions;
    }

    public String getDisplayPhone() {
        return displayPhone;
    }

    public String getPrice() {
        return price;
    }

    public boolean isIsClosed() {
        return isClosed;
    }

    public String getDelivery() {
        return delivery;
    }
    
    public String getPickup() {
        return pickup;
    }

    public String getClosed() {
        return this.closed;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==null) {
            return false;
        }
        if (!(o instanceof YelpBusiness)) {
            return false;
        }
        YelpBusiness compared = (YelpBusiness) o;
        
        return (this.name.equals(compared.name) &&
                this.rating == compared.rating &&
                this.reviewCount == compared.reviewCount &&
                (this.url == null && compared.url == null)||(this.url.equals(compared.url)) &&
                this.delivery.equals(compared.delivery) &&
                this.pickup.equals(compared.pickup) &&
                this.displayPhone.equals(compared.displayPhone) &&
                this.price.equals(compared.price) &&
                this.closed.equals(compared.closed)
               );
    
    }
}
