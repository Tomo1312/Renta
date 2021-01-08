package com.example.partyrental.Model;

public class HouseCard {
    String city;
    String picture;
    String address;
    String id;
    String description;
    String county;

    String ownerName;
    String ownerPhone;

    int price;

    public HouseCard() {
    }

    public HouseCard(String city, String picture, String address, String description, String ownerPhone, String ownerName, int price) {
        this.city = city;
        this.picture = picture;
        this.address = address;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.price = price;
    }

    public HouseCard(String city, String address, String description, int price, String county) {
        this.city = city;
        this.address = address;
        this.description = description;
        this.price = price;
        this.county = county;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }
}
