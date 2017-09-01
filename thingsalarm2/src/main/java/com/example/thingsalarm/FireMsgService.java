package com.example.thingsalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;



public class FireMsgService extends FirebaseMessagingService{


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("Msg", "Message recieved ["+remoteMessage+"]");

        //Create Notification
        Intent intent = new Intent(this,MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =PendingIntent.getActivity(this,1410,
                intent, PendingIntent.FLAG_ONE_SHOT);

        String info = null;

        if (remoteMessage.getData().size()>0) {
            info = remoteMessage.getData().get("message");
        }
        if (remoteMessage.getNotification() != null) {
            info = remoteMessage.getNotification().getBody();
        }

       Notification notification = new Notification.Builder(this)
               .setContentTitle("Message")
               .setContentText(remoteMessage.getNotification().getBody())
               .setSmallIcon(R.drawable.ic_stat_name)
               .setContentIntent(pendingIntent)
               .setAutoCancel(true).build();

        /* NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Message")
                .setContentText(info)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);*/


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(1410, notification);
    }
}
