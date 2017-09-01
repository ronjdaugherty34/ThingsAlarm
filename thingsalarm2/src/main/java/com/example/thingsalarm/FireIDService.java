package com.example.thingsalarm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;



public class FireIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh () {
        String tkn = FirebaseInstanceId.getInstance().getToken();
        Log.d("Not", "Toke ["+tkn+"]");




    }
}
