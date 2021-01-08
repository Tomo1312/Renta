package com.example.partyrental.Retrofit;

import io.reactivex.Observable;

import com.example.partyrental.Notification.FCMResponse;
import com.example.partyrental.Notification.FCMSendData;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAqgUtZoQ:APA91bETB34fKwB9T9bcOrO4jnwqutUl03i-S6wYgXC5Qqvf1aT5qJx2M9VRiCjIawFB06Udp8MPXkLW6c4lek_HNNf_T5oY_BMgJH4bAK7mlwAKtu9hSea0PshrpLN-l1htaN5h-g5Z"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
