package com.example.partyrental.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partyrental.Common.Common;
import com.example.partyrental.HouseActivity;
import com.example.partyrental.Interface.IRecycleItemSelectedListener;
import com.example.partyrental.Interface.IReservationDeleteListener;
import com.example.partyrental.Model.HouseCard;
import com.example.partyrental.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.MyViewHolder> {

    Context context;
    List<HouseCard> houseCardList;
    List<CardView> cardViewList;

    public HouseAdapter(Context context, List<HouseCard> houseCardList) {
        this.context = context;
        this.houseCardList = houseCardList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public HouseAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_card_home, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseAdapter.MyViewHolder holder, int position) {
        holder.txt_house_town.setText(houseCardList.get(position).getAddress() + ", " + houseCardList.get(position).getCity());
        holder.txt_house_price.setText(houseCardList.get(position).getPrice() + context.getResources().getString(R.string.add_to_price));
        try {
            Picasso.get().load(houseCardList.get(position).getPicture()).into(holder.house_image);
        }catch (Exception ex){
        }


        if (!cardViewList.contains(holder.house_card)) {
            cardViewList.add(holder.house_card);
        }
        holder.setIRecycleItemSelctedListener(new IRecycleItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                Common.selectedHouse = houseCardList.get(position);
                Intent staffHome = new Intent(context, HouseActivity.class);

                staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(staffHome);
            }
        });

    }

    @Override
    public int getItemCount() {
        return houseCardList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView house_card;

        TextView txt_house_town, txt_house_price;

        ImageView house_image;

        Button btn_reservation;
        IRecycleItemSelectedListener iRecycleItemSelectedListener;

        public void setIRecycleItemSelctedListener(IRecycleItemSelectedListener iRecycleItemSelctedListener) {
            this.iRecycleItemSelectedListener = iRecycleItemSelctedListener;
        }

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            house_card = itemView.findViewById(R.id.card_house);
            txt_house_town = itemView.findViewById(R.id.txt_house_town);
            txt_house_price = itemView.findViewById(R.id.txt_house_price);
            house_image = itemView.findViewById(R.id.house_image);
            btn_reservation = itemView.findViewById(R.id.btn_reservation);
//
//            btn_reservation.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CustomReservationDialog.getInstance().showReservationGuide(context);
//                }
//            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecycleItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
