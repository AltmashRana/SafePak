package com.example.safepak.frontend.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.logic.models.Constants
import com.google.android.gms.location.*
import android.os.HandlerThread
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.session.EmergencySession.updateUserLocationInFirebase


class LocationService : Service() {
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "safepak.notifications"
    private val description = "location notification"


    val DEFAULT_UPDATE_INTERVAL = 10
    val FAST_UPDATE_INTERVAL = 5

    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    val context = this


    override fun onCreate() {
        super.onCreate()

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
                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0].getAddressLine(0).toString()
                    val result = UserLocation(locationResult.lastLocation.longitude.toString(), locationResult.lastLocation.longitude.toString(), address)
                    updateUserLocationInFirebase(result)
                    Toast.makeText(applicationContext, "Location updated", Toast.LENGTH_SHORT).show()
                } catch (e : NullPointerException) {
                    Toast.makeText(applicationContext, "Geo-Api Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action;
            if (action != null) {
                if (action == Constants.ACTION_START_LOCATION_SERVICE) {
                    startLocationUpdates()
                } else if (action == Constants.ACTION_STOP_LOCATION_SERVICE) {
                    stopLocationUpdates();
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
                notificationChannel.lightColor = R.color.app_blue
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)


                builder = Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Location Service")
                    .setColor(ContextCompat.getColor(this, R.color.app_blue))
                    .setColorized(true)
                    .setContentText("Sharing location (Level-1 Alert)")
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.location_ic))
            } else {
                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Location Service")
                    .setContentText("Sharing location (Level-1 Alert)")
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

//    private fun updateGPS() {
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        ) {
//            fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location ->
//                updateUserLocationInFirebase(location)
//            }
//        }
//    }
}
