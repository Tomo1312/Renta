package com.example.partyrental.Interface;

import com.example.partyrental.Model.BookingInformation;

import java.util.List;

public interface IHouseReservationLoadListener {
    void onHouseReservationSuccess(List<BookingInformation> reservations);
    void onHouseReservationEmpty();
    void onHouseReservationFailed(String message);
}
