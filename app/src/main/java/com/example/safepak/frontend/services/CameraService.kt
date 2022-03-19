package com.example.safepak.frontend.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.media.MediaRecorder
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import com.example.safepak.R
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException

class CameraService : Service(), SurfaceHolder.Callback {
    private var windowManager: WindowManager? = null
    private var surfaceView: SurfaceView? = null
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    override fun onCreate() {

        // Start foreground service to avoid unexpected kill
        notification()

//        Create new SurfaceView, set its size to 1x1, move it to the top left
//        corner and set this service as a callback
        windowManager = this.getSystemService(WINDOW_SERVICE) as WindowManager
        surfaceView = SurfaceView(applicationContext)
        val layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
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


    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "Camera-Notifications"
    private val description = "Camera notification"


    private fun notification() {
        notificationManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentTitle("Camera Service")
        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentTitle("Camera Service")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            startForeground(4321, builder.build(),ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        else
            startForeground(4321, builder.build())
    }

    var isFrontFacing = true
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
            imagesFolder, System.currentTimeMillis()
                .toString() + ".mp4"
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

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        mediaRecorder!!.release()
        camera!!.lock()
        camera!!.release()
        windowManager!!.removeView(surfaceView)
        stopSelf()
    }


    private fun checkFrontCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FRONT
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras
        val action = intent.action
        if (action == "stop")
            onDestroy()
//      you can pass using intent,that which camera you want to use front/rear
        isFrontFacing = extras!!.getBoolean("Front_Request")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}
}