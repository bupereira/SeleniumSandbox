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
public class BBBBusiness {
    private String name;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String rating; /// BBB rates A+ to F, scale goes F,D-,D,D+,C-,C,C+,B-,B,B+,A-,A,A+
    
    public BBBBusiness(String[] dataRecord) {
        this.name = dataRecord[0];
        this.phone = dataRecord[1];
        this.address = dataRecord[2];
        this.city = dataRecord[3].split(",")[0];
        this.state = dataRecord[3].split(" ")[1];
        this.zip = dataRecord[3].split(" ")[2];
        this.rating = dataRecord[5];
    }
    
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        
        if (!(o instanceof BBBBusiness)) {
            return false;
        }
        
        BBBBusiness compared = (BBBBusiness) o;
        return (this.name.equals(compared.name) &&
                this.phone.equals(compared.phone) &&
                this.address.equals(compared.address) &&
                this.city.equals(compared.city) &&
                this.state.equals(compared.state) &&
                this.zip.equals(compared.zip) &&
                this.rating.equals(compared.rating) 
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
    
    
}
