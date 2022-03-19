package com.example.safepak.frontend.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.frontend.chat.ChatBoxActivity
import com.example.safepak.frontend.maps.LocationActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.net.Uri
import android.media.AudioAttributes
import com.example.safepak.frontend.blood.BloodResponseActivity
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.frontend.safety.Level2ResponseActivity
import com.example.safepak.logic.models.UserLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class FirebaseMessagingService : FirebaseMessagingService() {
    lateinit var builder: Notification.Builder
    private val chatchannel = "Chat-Notifications"
    private val level1channel = "Level1-Notifications"
    private val medicalchannel = "Medical-Notifications"
    private val level2channel = "Level2-Notifications"
    private val description1 = "chatNotifications"
    private val description2 = "level1Notifications"
    private val description3 = "level2Notifications"
    lateinit var intent : Intent

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val id = message.data["userid"].toString()

        FirebaseFirestore.getInstance().collection("users").document(id)
            .get().addOnSuccessListener {

                val user = it.toObject(User::class.java)

                when (message.data["type"]) {
                    "chat" -> {
                        FirebaseSession.getCurrentUser { me ->
                            if (me.status["state"] == "offline" && message.data["isfriend"].toBoolean()) {
                                sendChatNotification(
                                    user!!,
                                    message.data["body"]!!,
                                    message.data["title"]!!,
                                    message.data["isfriend"].toBoolean()
                                )
                            }
                        }
                    }
                    "level1" -> {
                        sendNotificationLevel1(
                            user!!,
                            message.data["body"]!!,
                            message.data["title"]!!,
                            message.data["callid"]!!,
                            message.data["isfriend"].toBoolean()
                        )
                    }

                    "level2" -> {
                        sendNotificationLevel2(
                            user!!,
                            message.data["body"]!!,
                            message.data["title"]!!,
                            message.data["callid"]!!,
                            message.data["isclose"].toBoolean(),
                            message.data["isfriend"].toBoolean()
                        )
                    }

                    "medical" -> {
                        getLocationAndNotify(user!!,message)
                    }
                }
            }
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, roundPixelSize: Int): Bitmap? {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = roundPixelSize.toFloat()
        paint.isAntiAlias = true
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    fun getLocationAndNotify(user : User, message: RemoteMessage){
        val ref = FirebaseDatabase.getInstance().getReference("/users-location/${user.userid}")

        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot: DataSnapshot in dataSnapshot.children) {
                    val location = snapshot.getValue(UserLocation::class.java)
                    if (location != null) {
                        sendNotificationMedical(
                            user,
                            message.data["body"]!!,
                            message.data["title"]!!,
                            location,
                            message.data["blood"]!!
                        )
                    }
                }
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    fun sendChatNotification(user : User, body : String, title : String, isfriend : Boolean){

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(chatchannel, description1, IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.gradient_green
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_KEY", user)
            intent.putExtra("IS_FRIEND", isfriend)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)



            builder = Notification.Builder(this, chatchannel)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.gradient_green))
                .setColorized(true)

        } else {
                builder = Notification.Builder(this)
                    .setSmallIcon(R.drawable.logo_ic)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(ContextCompat.getColor(this, R.color.gradient_green))
            }
        notificationManager.notify(5678, builder.build())
    }


    fun sendNotificationLevel1(user : User, body : String, title : String, callid: String, isfriend : Boolean){

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(level1channel, description2, IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.app_blue
            notificationChannel.setSound( Uri.parse(
                "android.resource://"
                        + applicationContext.packageName + "/" + R.raw.level1), attributes)
            notificationManager.createNotificationChannel(notificationChannel)

            intent = Intent(this, LocationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("call_id", callid)
            intent.putExtra("is_friend", isfriend)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)


            builder = Notification.Builder(this, level1channel)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentText(body)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level1_ic))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.app_blue))
                .setColorized(true)

        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.logo_ic)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level1_ic))
                .setSound( Uri.parse(
                    "android.resource://"
                            + applicationContext.packageName + "/" + R.raw.level1))
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ContextCompat.getColor(this, R.color.app_blue))
        }

        notificationManager.notify(333, builder.build())
    }

    fun sendNotificationMedical(user : User, body : String, title : String, location: UserLocation, blood : String){

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(medicalchannel, description1, IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.app_red
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            intent = Intent(this, BloodResponseActivity::class.java)
            intent.putExtra("USER", user)
            intent.putExtra("LOCATION", location)
            intent.putExtra("BLOOD", blood)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)


            builder = Notification.Builder(this, medicalchannel)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentText(body)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.medical_ic))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.app_red))
                .setColorized(true)

        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ContextCompat.getColor(this, R.color.app_red))
        }
        notificationManager.notify(5678, builder.build())
    }

    fun sendNotificationLevel2(user : User, body : String, title : String, callid : String, isclose : Boolean, isfriend : Boolean){

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(level2channel, description3, IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.app_red
            notificationChannel.enableVibration(true)
            notificationChannel.setSound( Uri.parse(
                "android.resource://"
                        + applicationContext.packageName + "/" + R.raw.level2), attributes)
            notificationManager.createNotificationChannel(notificationChannel)

            if (isclose) {
                intent = Intent(this, LocationActivity::class.java)
                intent.putExtra("user", user)
                intent.putExtra("is_friend", true)
                intent.putExtra("call_id", callid)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            } else{
                intent = Intent(this, Level2ResponseActivity::class.java)
                intent.putExtra("user", user)
                intent.putExtra("is_friend", isfriend)
                intent.putExtra("call_id", callid)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)


            builder = Notification.Builder(this, level2channel)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentText(body)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.level2_ic))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.app_red))
                .setColorized(true)

        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.logo_ic)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ContextCompat.getColor(this, R.color.app_red))
        }
        notificationManager.notify(5678, builder.build())
    }
}