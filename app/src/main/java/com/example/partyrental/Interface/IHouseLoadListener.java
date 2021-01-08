package com.example.partyrental.Interface;

import com.example.partyrental.Model.HouseCard;

import java.util.List;

public interface IHouseLoadListener {
    void onHouseLoadSuccess(List<HouseCard> houseCard);
    void onHouseLoadFailed(String message);
}
