package com.example.partyrental.Adapter;

import com.example.partyrental.Model.HouseImage;

import java.util.List;

import customSlider.adapters.SliderAdapter;
import customSlider.viewholder.ImageSlideViewHolder;


public class MySliderAdapter extends SliderAdapter {
    List<HouseImage> houseImageList;

    public MySliderAdapter(List<HouseImage> houseImageList) {
        this.houseImageList = houseImageList;
    }

    @Override
    public int getItemCount() {
        return houseImageList.size();
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
        imageSlideViewHolder.bindImageSlide(houseImageList.get(position).getImage());

    }
}
