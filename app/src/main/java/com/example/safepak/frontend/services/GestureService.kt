package com.example.safepak.frontend.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media.VolumeProviderCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.safepak.R
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.session.EmergencySession
import com.example.safepak.logic.session.EmergencySession.isLocationEnabled
import com.example.safepak.logic.session.EmergencySession.requestLocation
import com.example.safepak.logic.session.EmergencySession.startLocationService
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_safety.*
import java.lang.Exception
import kotlin.math.abs

import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest


class GestureService : Service() {

    lateinit var mediaSession: MediaSessionCompat // you have to initialize it in your onCreate method

    private lateinit var level2_player: MediaPlayer
    private lateinit var stopped_player: MediaPlayer

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "Gesture-Notifications"
    private val description = "Gesture notification"


    private val interval = 250L
    private var upTime = 9999L
    private var downTime = -9L


    private var level2Cancel_flag = false
    private var level2_flag = false


    private var gesture_count = 0
    private var gesture_gap = 9999L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        level2_player = MediaPlayer.create(this, R.raw.level2call)
        stopped_player = MediaPlayer.create(this, R.raw.stopped)

        notification()

        mediaSession = MediaSessionCompat(this, "PlayerService")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_PLAYING,
                0,
                0f
            ) //you simulate a player which plays something.
                .build()
        )

        val myVolumeProvider: VolumeProviderCompat = object : VolumeProviderCompat(
            VOLUME_CONTROL_RELATIVE,  /*max volume*/
            100,  /*initial volume level*/
            50
        ) {
            override fun onAdjustVolume(direction: Int) {

                when (direction) {
                    -1 -> {
                        downTime = SystemClock.elapsedRealtime()
//                        Toast.makeText(this@GestureService, "Down ${downTime}", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        upTime = SystemClock.elapsedRealtime()
//                        Toast.makeText(this@GestureService, "Up ${upTime}", Toast.LENGTH_SHORT).show()
                    }
                }

                if (direction != 0) {
                    val gap = abs((upTime - downTime))

                    if (gap < interval) {
                        gesture_count++

                        upTime = 9999L
                        downTime = -9L

                        if (gesture_count == 2) {
                            if (isLocationEnabled(this@GestureService)) {
                                if (ContextCompat.checkSelfPermission(this@GestureService, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
//                                    requestLocation(this@GestureService)
//                                    updateGPS { result ->
//                                        vibratePhone()
//                                        startLocationService(this@GestureService)
//                                        level2Cancel_flag = true
//                                        if (result != null) {
//                                            Thread {
//                                                Thread.sleep(5000)
//                                                Runnable {
//                                                    if (level2Cancel_flag) {
//                                                        EmergencySession.initiateLevel2(this@GestureService, result)

                                    if (ContextCompat.checkSelfPermission(this@GestureService, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(this@GestureService, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(this@GestureService, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                                      && ContextCompat.checkSelfPermission(requireView().context, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED
                                    )   {
                                            val cam_intent = Intent(this@GestureService, CameraService::class.java)
                                            cam_intent.putExtra("Front_Request", true)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                                startForegroundService(cam_intent)

                                        }
                                        level2Cancel_flag = false
                                        level2_flag = true
                                        level2_player.start()
//                                                    }
//                                                }.run()
//                                            }.start()
//                                        }
//                                    }
                                }
                            }
                        } else if (gesture_count == 3) {
                            if (level2_flag) {
                                gesture_count = 0
                                level2_flag = false
                                val cam_intent = Intent(this@GestureService, CameraService::class.java)
                                stopService(cam_intent)
//                                EmergencySession.stopCall(this@GestureService)
//                                EmergencySession.stopLocationService(this@GestureService)
                                stopped_player.start()
                                vibratePhone()
                            } else if (level2Cancel_flag) {
                                level2Cancel_flag = false
                            } else
                                gesture_count = 0
                        }
                    }
                }
            }
        }

        mediaSession.setPlaybackToRemote(myVolumeProvider)
        mediaSession.isActive = true

        return super.onStartCommand(intent, flags, startId)
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(300) // Vibrate method for below API Level 26
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.isActive = false
        mediaSession.release()
    }

    private fun updateGPS(onComplete: (UserLocation?) -> Unit) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@GestureService)
        //we have permission from user
        var result : UserLocation? = null
        if (ContextCompat.checkSelfPermission(this@GestureService, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener{ location ->
                val geocoder = Geocoder(this@GestureService)
                try {
                    if(location != null) {
                        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0].getAddressLine(0).toString()
                        result = UserLocation(location.longitude.toString(), location.latitude.toString(), address)
                        EmergencySession.updateUserLocationInFirebase(result)
                        onComplete(result)
                    }
                } catch (e: Exception) {
                    gesture_count = 0
                }
            }
    }

    private fun notification() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Gesture Service")
                    .setContentText("Off Screen gestures support")
            } else {
                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Gesture Service")
                    .setContentText("Off Screen gestures support")
            }
        startForeground(1234, builder.build())
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}