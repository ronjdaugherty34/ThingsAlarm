package com.example.ronalddaugherty.thingsalarm


import android.app.Activity

import android.content.ContentValues

import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread

import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.google.android.things.contrib.driver.button.ButtonInputDriver
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.io.IOException



class MainActivity : Activity() {



   

    //private  var  mSpeaker: Speaker? = null
    private lateinit var mButtonInputDriver: ButtonInputDriver
    private lateinit var mHandlerThread: HandlerThread
    private lateinit var mHandler: Handler
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mCamera: DoorbellCamera
    private lateinit var mCameraHandler: Handler
    private lateinit var mCameraThread: HandlerThread
    private lateinit var mCloudHandler: Handler
    private lateinit var mCloudThread: HandlerThread



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity created.")

        mDatabase = FirebaseDatabase.getInstance()

        mCameraThread = HandlerThread("CameraBackground")
        mCameraThread.start()
        mCameraHandler = Handler(mCameraThread.looper)

        mCloudThread = HandlerThread("CloudThread")
        mCloudThread.start()
        mCloudHandler = Handler(mCloudThread.looper)


       // mHandlerThread = HandlerThread("pwm-playback")
       // mHandlerThread.start()
       // mHandler = Handler(mHandlerThread.looper)
       // mHandler.post(mPlaybackRunnable)


        mCamera = DoorbellCamera.instance
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener)


        val service = PeripheralManagerService()




        // Init button

            try {
                mButtonInputDriver = ButtonInputDriver(BoardDefaults.gpioForButton,
                        com.google.android.things.contrib.driver.button.Button.LogicState.PRESSED_WHEN_LOW,
                        KeyEvent.KEYCODE_SPACE)
                mButtonInputDriver.register()

            }catch (e: IOException) {
                Log.e(TAG, "button driver error", e)

            }




        /*//find the pwnPin for speaker
            try {
                mSpeaker = Speaker(BoardDefaults.pwmPin)
                mSpeaker!!.stop()


            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Error initializing speaker")
                return
            }
        */


    }

    fun ringBell (view: View) {
        mCamera.takePicture()
       //l mHandler.post(mPlaybackRunnable)

    }



    override fun onDestroy() {
        super.onDestroy()
        mCamera.shutDown()
        mCameraThread.quitSafely()
        mCloudThread.quitSafely()
        mButtonInputDriver.unregister()

        Log.d(TAG, "onDestroy")
        



      /*  try {
            mSpeaker!!.stop()
            mSpeaker!!.close()
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "Error closing speaker")
        } */

        try {
            mButtonInputDriver.close()
        }catch (e: IOException) {
            Log.e(TAG, "Error closing Button driver")
        }
    }
    

   /* private val mPlaybackRunnable = object : Runnable {
        private var index = 0

        override fun run() {

            try {
                if (index == MusicNotes.DRAMATIC_THEME.size) {
                    //reached the end
                    mSpeaker!!.stop()
                } else {
                    val note = MusicNotes.DRAMATIC_THEME[index++]
                    if (note > 0) {
                        mSpeaker!!.play(note)
                    } else {
                       mSpeaker!!.stop()
                    }
                    mHandler.postDelayed(this, PLAYBACK_NOTE_DELAY)
                }
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Error playing speaker", e)
            }

        }
    } */

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

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        // Handle key events from captouch inputs
        if (keyCode == KeyEvent.KEYCODE_SPACE) {


            return true
        }
            return super.onKeyUp(keyCode, event)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            mCamera.takePicture()
            //mHandler.post(mPlaybackRunnable)

            return true
        }
        return super.onKeyDown(keyCode, event)
    }



    companion object {

        private val TAG = MainActivity::class.java.simpleName

        const val PLAYBACK_NOTE_DELAY = 80L
    }
}









