package com.example.ronalddaugherty.thingsalarm


import android.app.Activity
import android.app.NotificationManager
import android.content.ContentValues
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.constraint.ConstraintSet
import android.support.constraint.ConstraintLayout;
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Base64
import android.util.Log
import com.google.android.things.contrib.driver.pwmspeaker.Speaker

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.File

import java.io.IOException
import java.nio.ByteBuffer


class MainActivity : Activity() {



    private lateinit var gpioPin: Gpio
    private lateinit var  mSpeaker: Speaker
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandler: Handler
    private lateinit var sensorCallBack: SensorCallBack
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mCamera: DoorbellCamera
    private lateinit var mCameraHandler: Handler
    private lateinit var mCameraThread: HandlerThread
    private lateinit var mCloudHandler: Handler
    private lateinit var mCloudThread: HandlerThread

    private var mConstraintSet1 =ConstraintSet()
    private var mConstraintSet2 = ConstraintSet()
    private var mConstraintLayout: ConstraintLayout? = null
    // private var mOld = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val context = this

        mConstraintSet2.clone(mConstraintLayout)
        mConstraintSet2.centerVertically(R.id.image, 0)

        var changed =false


        button1.setOnClickListener{
            val transition = AutoTransition()
            transition.duration = 1000
            TransitionManager.beginDelayedTransition(mConstraintLayout, transition)
            val constraint = if (changed)
                mConstraintSet1 else
                mConstraintSet2
            constraint.applyTo(mConstraintLayout)
            changed = !changed


            toast("Clicked")}





        Log.d(TAG, "MainActivity created.")





        mDatabase = FirebaseDatabase.getInstance()

        mCameraThread = HandlerThread("CameraBackground")
        mCameraThread.start()
        mCameraHandler = Handler(mCameraThread.looper)

        mCloudThread = HandlerThread("CloudThread")
        mCloudThread.start()
        mCloudHandler = Handler(mCloudThread.looper)

        mCamera = DoorbellCamera.instance
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener)


        val service = PeripheralManagerService()

        //find the pwnPin for speaker
            try {
                mSpeaker = Speaker(BoardDefaults.pwmPin)
                mSpeaker.stop()


            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Error initializing speaker")
                return
            }

        mHandlerThread = HandlerThread("pwm-playback")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        mHandler.post(mPlaybackRunnable)




            try {
                gpioPin = service.openGpio(BoardDefaults.pirPin)
                gpioPin.setDirection(Gpio.DIRECTION_IN)
                gpioPin.setActiveType(Gpio.ACTIVE_HIGH)

                val status = gpioPin.value
                Log.d(TAG, "Status [$status]")

                sensorCallBack = SensorCallBack()
                gpioPin.setEdgeTriggerType(Gpio.EDGE_RISING)
                gpioPin.registerGpioCallback(sensorCallBack)

                (Thread(Runnable {
                    try {
                        val status = gpioPin.value
                        Log.d(TAG, "Sensor status [" + status + "]")
                        if (status) {
                            Log.i(TAG, "Motion detected...")
                            mCamera.takePicture()
                            Log.i(TAG, "Pic Taken bastard!!")

                        }
                        Thread.sleep(5000)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })).start()


            } catch (ioe: IOException) {

            }
    }


    override fun onDestroy() {
        super.onDestroy()
        mCamera.shutDown()
        mCameraThread.quitSafely()
        mCloudThread.quitSafely()

        Log.d(TAG, "onDestroy")

        gpioPin.unregisterGpioCallback(sensorCallBack)
        try {
            gpioPin.close()

        } catch (e: Exception) {
        }


        try {
            mSpeaker.stop()
            mSpeaker.close()
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "Error closing speaker")
        }
    }

    private val mPlaybackRunnable = object : Runnable {
        private var index = 0

        override fun run() {

            try {
                if (index == MusicNotes.DRAMATIC_THEME.size) {
                    // reached the end
                    mSpeaker.stop()
                } else {
                    val note = MusicNotes.DRAMATIC_THEME[index++]
                    if (note > 0) {
                        mSpeaker.play(note)
                    } else {
                        mSpeaker.stop()
                    }
                    mHandler.postDelayed(this, PLAYBACK_NOTE_DELAY)
                }
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Error playing speaker", e)
            }

        }
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { imageReader ->
        val image = imageReader.acquireLatestImage()
        // get image bytes
        val imageBuf = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuf.remaining())
        imageBuf.get(imageBytes)
        image.close()
        onPictureTaken(imageBytes)
    }

    private fun onPictureTaken(imageBytes: ByteArray?) {

        Log.d(TAG, "in pic taken")


        imageBytes?.let {
            Log.d(TAG, "Please work")
            val log = mDatabase.getReference("logs").push()
            val imageStr = Base64.encodeToString(imageBytes, Base64.NO_WRAP or Base64.URL_SAFE)
            //upload image to firebase
            log.child("timestamp").setValue(ServerValue.TIMESTAMP)
            log.child("image").setValue(imageStr)

            mCloudHandler.post {
                Log.d(TAG, "sending image to cloud vision")
                // annotate image by uploading to Cloud Vision API
                try {
                    Log.d(TAG, "What the fuck dude:")
                    val annotations = CloudVisionUtils.annotateImage(imageBytes)
                    Log.d(TAG, "cloud vision annotations:" + annotations)



                    if (annotations != null) {
                        log.child("annotations").setValue(annotations)
                    } else {
                        Log.d(TAG, "Annotations are null")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Cloud Vison API error: ", e)
                }
            }

    }
        Log.d(TAG, "whats going on")
    }




    private inner class SensorCallBack : GpioCallback() {

        override fun onGpioEdge(gpio: Gpio?): Boolean {
            try {
                val callBackState = gpio!!.value
                Log.d(TAG, "Callback state [$callBackState]")
                mCamera.takePicture()
                mHandler.post(mPlaybackRunnable)

                com.example.ronalddaugherty.thingsalarm.NotificationManager
                        .instance.sendNotification( "Alarm!",
                "AAAA0ADwj3o:APA91bHXU7NLVABqSlXk1HIwqR4o9jPdtPZBfAHZw66WAeruyjjnp8RjlGXVCK1sg1mfQLX5-oAjA-XtvWCbShSWW9AkzrWMD_ufNZzdE89Vu1OMbNf03oJvqMgXkj1A9kbvDDephf7O")

            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }

            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            super.onGpioError(gpio, error)
        }
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName
        const val PLAYBACK_NOTE_DELAY = 80L
    }
}



