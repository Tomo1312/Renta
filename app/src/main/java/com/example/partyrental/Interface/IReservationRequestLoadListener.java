package com.example.partyrental.Interface;

import com.example.partyrental.Model.BookingInformation;

import java.util.List;

public interface IReservationRequestLoadListener {
    void onReservationRequestSuccess(List<BookingInformation> reservations);
    void onReservationRequestEmpty();
    void onReservationRequestFailed(String message);
}
