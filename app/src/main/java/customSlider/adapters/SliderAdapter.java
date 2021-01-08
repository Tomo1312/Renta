package customSlider.adapters;

import customSlider.SlideType;
import customSlider.viewholder.ImageSlideViewHolder;

public abstract class SliderAdapter {
    public abstract int getItemCount();

    public final SlideType getSlideType(int position) {
        return SlideType.IMAGE;
    }

    public abstract void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder);
}