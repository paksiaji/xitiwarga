package com.example.xiti_nganjuk_v2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CommentNotificationService extends FirebaseMessagingService {
    private static final String TAG = CommentNotificationService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message = remoteMessage.getData().get("text");
        String extra = "";
        if(message.contains("Anda Mempunyai Pesan Baru Dari")){
            extra = remoteMessage.getData().get("userId");
        }else if(message.contains("Mengomentari Laporan Anda")){
            extra = remoteMessage.getData().get("postId");
        }
        sendNotification(message,extra);
    }

    private void sendNotification(String message,String extra){
        Intent intent = null;
        if(message.contains("Anda Mempunyai Pesan Baru Dari")) {
            intent = new Intent(this,ChatRoomActivity.class);
            intent.putExtra("userId", extra);
        }else if(message.contains("Mengomentari Laporan Anda")){
            intent = new Intent(this,CommentActivity.class);
            intent.putExtra("postId",extra);
        }else{
            intent = new Intent(this,MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Xiti Apps")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());
    }
}
