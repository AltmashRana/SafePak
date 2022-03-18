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


class LocationService : Service() , SurfaceHolder.Callback{
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
                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0].getAddressLine(0).toString()
                    val result = UserLocation(locationResult.lastLocation.longitude.toString(), locationResult.lastLocation.latitude.toString(), address)
                    updateUserLocationInFirebase(result)
                    Toast.makeText(applicationContext, "Location updated", Toast.LENGTH_SHORT).show()
                } catch (e : NullPointerException) {
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
                    if (call_type == "level2")
                        isFrontFacing = intent.extras!!.getBoolean("front_cam")
                        recordVideo()
                    startLocationUpdates()
                } else if (action == Constants.ACTION_STOP_LOCATION_SERVICE) {
                    if (call_type == "level2")
                        stopRecording()
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
                notificationChannel.lightColor = R.color.app_red
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)


                builder = Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Emergency Service")
                    .setColorized(true)
                        if (call_type == "level2"){
                            builder
                                .setColor(ContextCompat.getColor(this, R.color.app_blue))
                                .setContentText("Level-1 Active (Sharing location)")
                                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level1))
                        } else if (call_type == "level1"){
                            builder
                                .setColor(ContextCompat.getColor(this, R.color.app_red))
                                .setContentText("Level2 Active (Location and Camera)")
                                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level2))
                        }
            } else {
                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle("Emergency Service")
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

    private var windowManager: WindowManager? = null
    private var surfaceView: SurfaceView? = null
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null


    private fun recordVideo() {
        //Camera
        windowManager = this.getSystemService(WINDOW_SERVICE) as WindowManager
        surfaceView = SurfaceView(applicationContext)
        val layout_parms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val layoutParams = WindowManager.LayoutParams(
            1,
            1,
            layout_parms,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        windowManager!!.addView(surfaceView, layoutParams)
        surfaceView!!.holder.addCallback(this)
    }


    var isFrontFacing = false
    private fun openFrontFacingCameraGingerbread(): Camera? {
        if (camera != null) {
            camera!!.stopPreview()
            camera!!.release()
        }
        var cam: Camera? = null
        if (isFrontFacing && checkFrontCamera(this)) {
            var cameraCount = 0
            cam = null
            val cameraInfo = Camera.CameraInfo()
            cameraCount = Camera.getNumberOfCameras()
            for (camIdx in 0 until cameraCount) {
                Camera.getCameraInfo(camIdx, cameraInfo)
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        cam = Camera.open(camIdx)
                    } catch (e: RuntimeException) {
                        Log.e(
                            "Camera",
                            "Camera failed to open: " + e.localizedMessage
                        )
                    }
                }
            }
        } else {
            cam = Camera.open()
        }
        return cam
    }

//    private val pictureSize: Camera.Size? = null
//    private fun getBiggesttPictureSize(parameters: Camera.Parameters): Camera.Size? {
//        var result: Camera.Size? = null
//        for (size in parameters.supportedVideoSizes) {
//            if (result == null) {
//                result = size
//            } else {
//                val resultArea = result.width * result.height
//                val newArea = size.width * size.height
//                if (newArea > resultArea) {
//                    result = size
//                }
//            }
//        }
//        return result
//    }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        camera = openFrontFacingCameraGingerbread()
        mediaRecorder = MediaRecorder()
        camera!!.unlock()
        mediaRecorder!!.setPreviewDisplay(surfaceHolder.surface)
        mediaRecorder!!.setCamera(camera)
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P))
        val imagesFolder = File(
            getExternalFilesDir(null), "Safepak"
        )
        if (!imagesFolder.exists()) imagesFolder.mkdirs() // <----
        val image = File(
            imagesFolder, "Incident-${getDatetime()}" + ".mp4"
        ) //file name + extension is .mp4
        mediaRecorder!!.setOutputFile(image.absolutePath)
        try {
            mediaRecorder!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "media Recorder Failed", Toast.LENGTH_SHORT).show()
        }
        try {
            mediaRecorder!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "media Recorder Failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Stop recording and remove SurfaceView
     private fun stopRecording() {
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        mediaRecorder!!.release()
        camera!!.lock()
        camera!!.release()
        windowManager!!.removeView(surfaceView)
    }

    private fun checkFrontCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FRONT
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}
}
