package com.example.partyrental.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Interface.IReservationDeleteListener;
import com.example.partyrental.Model.BookingInformation;
import com.example.partyrental.Notification.FCMResponse;
import com.example.partyrental.Notification.FCMSendData;
import com.example.partyrental.Notification.MyNotification;
import com.example.partyrental.Notification.MyToken;
import com.example.partyrental.Notification.NotificationCommon;
import com.example.partyrental.R;
import com.example.partyrental.Retrofit.IFCMService;
import com.example.partyrental.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UserReservationAdapter extends RecyclerView.Adapter<UserReservationAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> reservations;
    List<CardView> cardViewList;

    public UserReservationAdapter(Context context, List<BookingInformation> reservations) {
        this.context = context;
        this.reservations = reservations;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserReservationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_reservation, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReservationAdapter.MyViewHolder holder, int position) {
        StringBuilder reservationDate = new StringBuilder();
        reservationDate.append(reservations.get(position).getStartDate()).append(" 10:00-").append(reservations.get(position).getEndDate()).append(" 10:00");

        holder.txt_house_town.setText(reservations.get(position).getHouseAddress() + ", " + reservations.get(position).getHouseCity());
        holder.txt_phone_number.setText(reservations.get(position).getOwnerPhone());
        holder.txt_time_reservation.setText(reservationDate.toString());
        Picasso.get().load(reservations.get(position).getHouseImage()).into(holder.house_image);


        if (!cardViewList.contains(holder.card_reservation)) {
            cardViewList.add(holder.card_reservation);
        }


        holder.setIReservationDeleteListener(new IReservationDeleteListener() {
            @Override
            public void onIReservationDeleteListener(View view, int position) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("Sigurno zelite otkazat?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteReservationFromHouse(position, "Rezervacija otkazana");
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });
    }

    private void deleteReservationFromHouse(int position, String notificationMessage) {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(reservations.get(position).getHouseId())
                .collection("Reservation")
                .document(reservations.get(position).getReservationId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deleteReservationFromUser(position, notificationMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReservationFromUser(int position, String notificationMessage) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getUserId())
                .collection("Reservation")
                .document(reservations.get(position).getReservationId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        NotificationCommon.sendNotification(reservations.get(position), notificationMessage, reservations.get(position).getOwnerPhone());
                        cardViewList.clear();
                        reservations.remove(position);
                        notifyItemRemoved(position);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {


        CardView card_reservation;

        ImageView house_image;

        TextView txt_house_town, txt_phone_number, txt_time_reservation;

        IReservationDeleteListener iReservationDeleteListener;

        public void setIReservationDeleteListener(IReservationDeleteListener iReservationDeleteListener) {
            this.iReservationDeleteListener = iReservationDeleteListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_reservation = itemView.findViewById(R.id.card_reservation);
            txt_house_town = itemView.findViewById(R.id.txt_house_town);
            txt_phone_number = itemView.findViewById(R.id.txt_phone_number);
            txt_time_reservation = itemView.findViewById(R.id.txt_time_reservation);
            house_image = itemView.findViewById(R.id.house_image);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            iReservationDeleteListener.onIReservationDeleteListener(v, getAdapterPosition());
            return false;
        }
    }
}
