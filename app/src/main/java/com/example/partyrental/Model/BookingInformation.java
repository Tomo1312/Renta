package com.example.partyrental.Model;

import com.google.firebase.Timestamp;
public class BookingInformation {

    private Timestamp startDateTimeStamp;
    private Timestamp endDateTimeStamp;
    private String startDate;
    private String endDate;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerId;
    private String houseAddress;
    private String houseCity;
    private String houseId;
    private String houseImage;
    private String reservationId;
    private String ownerPhone;
    private boolean done;


    public BookingInformation() {
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getHouseAddress() {
        return houseAddress;
    }

    public void setHouseAddress(String houseAddress) {
        this.houseAddress = houseAddress;
    }

    public String getHouseCity() {
        return houseCity;
    }

    public void setHouseCity(String houseCity) {
        this.houseCity = houseCity;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Timestamp getStartDateTimeStamp() {
        return startDateTimeStamp;
    }

    public void setStartDateTimeStamp(Timestamp startDateTimeStamp) {
        this.startDateTimeStamp = startDateTimeStamp;
    }

    public Timestamp getEndDateTimeStamp() {
        return endDateTimeStamp;
    }

    public void setEndDateTimeStamp(Timestamp endDateTimeStamp) {
        this.endDateTimeStamp = endDateTimeStamp;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getHouseImage() {
        return houseImage;
    }

    public void setHouseImage(String houseImage) {
        this.houseImage = houseImage;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

}
