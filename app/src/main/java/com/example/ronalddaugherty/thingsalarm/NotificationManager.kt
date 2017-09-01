package com.example.ronalddaugherty.thingsalarm

import android.os.AsyncTask
import android.util.Log

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL



class NotificationManager private constructor() {

    fun sendNotification(message: String, key: String) {

        FirebaseNotificationTask().execute(message, key)
    }

    private class FirebaseNotificationTask : AsyncTask<String, Void, Void>() {

        override fun doInBackground(vararg strings: String): Void? {
            val msg = strings[0]
            val key = strings[1]
            Log.d("Alm", "Send data")
            try {
                val con = URL("http://fcm.googleapis.com/fcm/send").openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.setRequestProperty("Authorization", "key=" + key)
                con.setRequestProperty("Content-Type", "application/json")
                con.doInput = true
                con.doOutput = true
                con.connect()

                val body = "{\n" +
                        "  \"to\": \"/topics/alarm\",\n" +
                        "  \"data\": {\n" +
                        "  \"message\": \"" + msg + "\"" +
                        "  }\n" +
                        "}"
                Log.d("Alm", "Body [$body]")
                con.outputStream.write(body.toByteArray())
                val `is` = con.inputStream
                val buffer = ByteArray(1024)
                while (`is`.read(buffer) != -1)
                    Log.d("Alm", String(buffer))
                con.disconnect()

            } catch (t: Throwable) {
                t.printStackTrace()
            }

            return null
        }
    }

    companion object {

        private var  notifManager: NotificationManager? = null

        val instance: NotificationManager
            get() {

                    notifManager = NotificationManager()

                return notifManager!!
            }
    }

}
