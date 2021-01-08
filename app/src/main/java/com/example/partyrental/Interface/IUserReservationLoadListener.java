package com.example.partyrental.Interface;

import com.example.partyrental.Model.BookingInformation;
import com.example.partyrental.Model.HouseCard;

import java.util.List;

public interface IUserReservationLoadListener {
    void onUserReservationSuccess(List<BookingInformation> reservations);
    void onUserReservationEmpty();
    void onUserReservationFailed(String message);
}
