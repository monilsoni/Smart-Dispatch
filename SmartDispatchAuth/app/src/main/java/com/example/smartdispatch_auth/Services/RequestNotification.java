package com.example.smartdispatch_auth.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UI.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.support.constraint.Constraints.TAG;

public class RequestNotification extends FirebaseMessagingService {

    private String authenticator = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> hashMap = remoteMessage.getData();
        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), hashMap);
    }

    private void showNotification(String title, String body, Map<String, String> hashMap) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_02";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("TEST");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info");

        getAuthenicator();


        switch (hashMap.get("user")) {
            case "hospital":
                if (authenticator.equals("hospital")){
                    notificationManager.notify(new Random().nextInt(), notificationBuilder.build());

                    if (hashMap.get("type").equals("connected")) {
                        Intent i = new Intent("send");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    } else {
                        Intent i = new Intent("vReach");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    }
                }


                break;
            case "requester":
                if (authenticator.equals("requester")){
                    notificationManager.notify(new Random().nextInt(), notificationBuilder.build());

                    if (hashMap.get("type").equals("connected")) {
                        Intent i = new Intent("vehicle_alloted");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    } else {
                        Intent i = new Intent("vehicle_reached");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    }

                }

                break;
            case "vehicle":
                if (authenticator.equals("vehicle")){
                    notificationManager.notify(new Random().nextInt(), notificationBuilder.build());

                    Intent i = new Intent("get");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                }

                break;

            default:
                Log.d(TAG, "showNotification: Error in matching the user");
        }


    }

    @Override
    public void onNewToken(String s) {

        getAuthenicator();

        Map<String, Object> token = new HashMap<>();
        token.put("token", s);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            if(authenticator.equals("hospital")){
                FirebaseFirestore.getInstance().collection(getString(R.string.collection_hospitals)).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(token, SetOptions.merge());

            }else if(authenticator.equals("vehicle")){
                FirebaseFirestore.getInstance().collection(getString(R.string.collection_vehicles)).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(token, SetOptions.merge());

            }else{
                FirebaseFirestore.getInstance().collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(token, SetOptions.merge());

            }
        }
    }

    private void getAuthenicator() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String email = user.getEmail();
            if(email == null)
                return;

            if(email.contains("@smartdispatch.gov.in")){
                if(email.contains("v_"))
                    authenticator = "vehicle";
                else
                    authenticator = "hospital";
            }else{
                authenticator = "requester";
            }

        }
    }
}
