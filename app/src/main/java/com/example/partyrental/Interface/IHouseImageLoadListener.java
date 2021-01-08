package com.example.partyrental.Interface;

import com.example.partyrental.Model.HouseImage;

import java.util.List;

public interface IHouseImageLoadListener {
    void onHouseImageLoadSuccess(List<HouseImage> banners);
    void onHouseImageLoadError(String message);
}
