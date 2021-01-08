package com.example.partyrental.Services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.partyrental.MainActivity;
import com.example.partyrental.Notification.NotificationCommon;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        NotificationCommon.updateToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationCommon.showNotification(this, new Random().nextInt(), remoteMessage.getData().get(NotificationCommon.TITLE_KEY), remoteMessage.getData().get(NotificationCommon.CONTENT_KEY), new Intent(this, MainActivity.class));
    }
}