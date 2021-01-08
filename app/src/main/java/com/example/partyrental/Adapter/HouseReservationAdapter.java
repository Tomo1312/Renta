package com.example.partyrental.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HouseReservationAdapter extends RecyclerView.Adapter<HouseReservationAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> reservations;
    List<CardView> cardViewList;

    public HouseReservationAdapter(Context context, List<BookingInformation> reservations) {
        this.context = context;
        this.reservations = reservations;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public HouseReservationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_house_reservation, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseReservationAdapter.MyViewHolder holder, int position) {
        StringBuilder reservationDate = new StringBuilder();
        reservationDate.append(reservations.get(position).getStartDate()).append(" 10:00 - ").append(reservations.get(position).getEndDate()).append(" 10:00");

        holder.txt_house_town.setText(reservations.get(position).getCustomerName() + ", " + reservations.get(position).getCustomerEmail());
        holder.txt_phone_number.setText(reservations.get(position).getCustomerPhone());
        holder.txt_time_reservation.setText(reservationDate.toString());


        if (!cardViewList.contains(holder.card_reservation)) {
            cardViewList.add(holder.card_reservation);
        }

        IReservationDeleteListener iReservationDeleteListener = new IReservationDeleteListener() {
            @Override
            public void onIReservationDeleteListener(View view, int position) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("Sigurno zelite otkazati?")
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
        };

        IReservationDeleteListener iAcceptReservation = new IReservationDeleteListener() {
            @Override
            public void onIReservationDeleteListener(View view, int position) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setPositiveButton("Prihvati rezervaciju", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        acceptReservationOnHouse(position);
                    }
                }).setNegativeButton("Odbij rezervaciju", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteReservationFromHouse(position, "Rezervacija odbijena!");
                    }
                });
                AlertDialog alert = dialog.create();
                alert.show();

            }
        };

        if (reservations.get(position).isDone())
            holder.setIReservationDeleteListener(iReservationDeleteListener);
        else
            holder.setIReservationDeleteListener(iAcceptReservation);

    }

    private void acceptReservationOnHouse(int position) {
        FirebaseFirestore.getInstance()
                .collection("Kuce")
                .document(reservations.get(position).getHouseId())
                .collection("Reservation")
                .document(reservations.get(position).getReservationId())
                .update("done", true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        acceptReservationOnUser(position);
                    }
                });
    }

    private void acceptReservationOnUser(int position) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(reservations.get(position).getCustomerId())
                .collection("Reservation")
                .document(reservations.get(position).getReservationId())
                .update("done", true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        NotificationCommon.sendNotification(reservations.get(position), "Rezervacija prihvacena!", reservations.get(position).getCustomerPhone());
                        cardViewList.clear();
                        reservations.remove(position);
                        notifyItemRemoved(position);
                    }
                });
    }

    private void sendNotificationAccepted(int position, String message) {

        String content = reservations.get(position).getStartDate() + " 10:00 - " +
                reservations.get(position).getEndDate() + " 10:00";


        MyNotification myNotification = new MyNotification();
        myNotification.setUid(UUID.randomUUID().toString());
        myNotification.setTitle(message);
        myNotification.setContent(content);
        myNotification.setRead(false); //We will only filter notification with 'read is false on barber staff
        myNotification.setServerTimestamp(FieldValue.serverTimestamp());
        String finalContent = content;
        FirebaseFirestore.getInstance().collection("Tokens")
                .whereEqualTo("user", reservations.get(position).getCustomerPhone())
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {
                            MyToken myToken = new MyToken();
                            for (DocumentSnapshot tokenSnapshot : task.getResult()) {
                                myToken = tokenSnapshot.toObject(MyToken.class);

                                FCMSendData sendRequest = new FCMSendData();
                                Map<String, String> dataSend = new HashMap<>();
                                dataSend.put(NotificationCommon.TITLE_KEY, message);
                                dataSend.put(NotificationCommon.CONTENT_KEY, finalContent);

                                sendRequest.setTo(myToken.getToken());
                                sendRequest.setData(dataSend);

                                CompositeDisposable compositeDisposable = new CompositeDisposable();


                                IFCMService ifcmApi =
                                        RetrofitClient.getInstance().create(IFCMService.class);
                                compositeDisposable.add
                                        (ifcmApi.sendNotification(sendRequest)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<FCMResponse>() {
                                                    @Override
                                                    public void accept(FCMResponse fcmResponse) throws Exception {
                                                        Common.selectedHouse = null;

                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        Log.d("NOTIFICATION_ERROR", throwable.getMessage());

                                                        cardViewList.clear();
                                                        reservations.remove(position);
                                                        notifyItemRemoved(position);
                                                        Common.selectedHouse = null;
                                                    }
                                                }));
                            }
                        }
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
                .document(reservations.get(position).getCustomerId())
                .collection("Reservation")
                .document(reservations.get(position).getReservationId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        NotificationCommon.sendNotification(reservations.get(position), notificationMessage, reservations.get(position).getCustomerPhone());
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

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            iReservationDeleteListener.onIReservationDeleteListener(v, getAdapterPosition());
            return false;
        }
    }
}
