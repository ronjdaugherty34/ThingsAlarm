package com.example.thingsalarm

import android.util.Log

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService


class FireIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val tkn = FirebaseInstanceId.getInstance().token
        Log.d("Not", "Toke [$tkn]")


    }

    companion object {

        private val TAG = "MyFirebaseIIDService"
    }
}
