package com.example.safepak.frontend.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.Camera
import android.location.Geocoder
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.logic.models.Constants
import com.google.android.gms.location.*
import android.os.HandlerThread
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.session.EmergencySession.getDatetime
import com.example.safepak.logic.session.EmergencySession.updateUserLocationInFirebase
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException


class LocationService : Service(){
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "Safepak-notifications"
    private val description = "emergency-notification"

    private lateinit var call_type : String

    val DEFAULT_UPDATE_INTERVAL = 10
    val FAST_UPDATE_INTERVAL = 5

    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    val context = this


    override fun onCreate() {
        super.onCreate()

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest!!.interval = (1000 * DEFAULT_UPDATE_INTERVAL).toLong()
        locationRequest!!.fastestInterval = (1000 * FAST_UPDATE_INTERVAL).toLong()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //this is triggered when update location interval is met (in our case, it is 30 seconds)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                val geocoder = Geocoder(this@LocationService)
                try {
                    var address = "Missing"
                    try {
                        address = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )[0].getAddressLine(0).toString()
                    }catch (e : IndexOutOfBoundsException) {
                        Toast.makeText(this@LocationService, "Address Failed", Toast.LENGTH_SHORT).show()
                    }
                    val result = UserLocation(locationResult.lastLocation.longitude.toString(), locationResult.lastLocation.latitude.toString(), address)
                    updateUserLocationInFirebase(result)
                    Toast.makeText(applicationContext, "Location updated", Toast.LENGTH_SHORT).show()
                } catch (e1 : NullPointerException) {
                    Toast.makeText(applicationContext, "Geo-Api Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action;
            call_type = intent.extras?.getString("call_type").toString()
            if (action != null) {
                if (action == Constants.ACTION_START_LOCATION_SERVICE) {
                    startLocationUpdates()
                } else if (action == Constants.ACTION_STOP_LOCATION_SERVICE) {
                    stopLocationUpdates()
                }
            }
        }
        return START_STICKY
    }


    @SuppressLint("ResourceAsColor")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val handlerThread = HandlerThread("backgroundThread")
            if (!handlerThread.isAlive) handlerThread.start()
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
               handlerThread.looper
            )
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = R.color.app_red
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)


                builder = Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Emergency Service")
                    .setColorized(true)
                        if (call_type == "level1"){
                            builder
                                .setColor(ContextCompat.getColor(this, R.color.app_blue))
                                .setContentText("Level-1 Active (Sharing location)")
                                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level1))
                        } else if (call_type == "level2"){
                            builder
                                .setColor(ContextCompat.getColor(this, R.color.app_red))
                                .setContentText("Level2 Active (Location and Camera)")
                                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level2))
                        }
            } else {
                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Emergency Service")
                    .setContentText("Sharing location")
            }

            startForeground(1122, builder.build())
        }
    }

    private fun stopLocationUpdates() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
        stopForeground(true)
        stopSelf()
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
