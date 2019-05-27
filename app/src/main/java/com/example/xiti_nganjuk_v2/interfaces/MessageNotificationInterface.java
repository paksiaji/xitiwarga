package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.NotificationResponseModell;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MessageNotificationInterface {
    @Headers({"Content-Type: application/json","Authorization: key=AIzaSyBPW0rIVWflMGw4SL9N-AT6QlftY6xtQYI"})
    @POST("/fcm/send")
    Call<NotificationResponseModell> sendNotification(
      @Body RequestBody body
    );
}
