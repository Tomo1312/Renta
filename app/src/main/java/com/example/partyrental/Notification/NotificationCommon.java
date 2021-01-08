package com.example.partyrental.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.partyrental.Common.Common;
import com.example.partyrental.Common.Constants;
import com.example.partyrental.Model.BookingInformation;
import com.example.partyrental.R;
import com.example.partyrental.Retrofit.IFCMService;
import com.example.partyrental.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NotificationCommon {
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "content";

    public static void updateToken(String s) {
        if (Common.currentUser != null) {
            MyToken myToken = new MyToken();
            myToken.setToken(s);
            myToken.setTokenType(TOKEN_TYPE.CLIENT);
            myToken.setUser(Common.currentUser.getMobile());

            FirebaseFirestore.getInstance()
                    .collection("Tokens")
                    .document(Common.currentUser.getMobile())
                    .set(myToken);
        }
    }

    public static void updateToken(Context context, String token) {

        //First we need check if user still login, we need to store token belonging user
        Paper.init(context);
        String user = Paper.book().read(Constants.LOGGED_KEY);
        if (user != null) {
            if (!TextUtils.isEmpty(user)) {
                MyToken myToken = new MyToken();
                myToken.setToken(token);
                myToken.setTokenType(TOKEN_TYPE.HOUSE_OWNER);
                myToken.setUser(user);

                FirebaseFirestore.getInstance().collection("Tokens").document(user)
                        .set(myToken)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                }
                            }
                        });
            }
        }
    }

    public static void showNotification(Context context, int notification_id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, notification_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "Party_rental_01";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Booking staff app", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("STAFF APP");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();

        notificationManager.notify(notification_id, notification);

    }

    public static void sendNotification(BookingInformation reservation, String notificationTitle, String phoneNumber) {
        String content = "Datum: " + reservation.getStartDate() + " - " + reservation.getEndDate();
        MyNotification myNotification = new MyNotification();
        myNotification.setUid(UUID.randomUUID().toString());
        myNotification.setTitle(notificationTitle);
        myNotification.setContent(content);
        myNotification.setRead(false); //We will only filter notification with 'read is false on barber staff
        myNotification.setServerTimestamp(FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("Tokens")
                .whereEqualTo("user", phoneNumber)
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
                                dataSend.put(NotificationCommon.TITLE_KEY, notificationTitle);
                                dataSend.put(NotificationCommon.CONTENT_KEY, content);

                                sendRequest.setTo(myToken.getToken());
                                sendRequest.setData(dataSend);

                                CompositeDisposable compositeDisposable = new CompositeDisposable();
                                IFCMService ifcmApi = RetrofitClient.getInstance().create(IFCMService.class);

                                compositeDisposable.add
                                        (ifcmApi.sendNotification(sendRequest)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<FCMResponse>() {
                                                    @Override
                                                    public void accept(FCMResponse fcmResponse) throws Exception {

                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        Log.d("NOTIFICATION_ERROR", throwable.getMessage());
                                                    }
                                                }));

                            }
                        }
                    }
                });
    }


    public enum TOKEN_TYPE{
        CLIENT,
        HOUSE_OWNER
    }
}
