package com.example.safepak.logic.session

import FirebaseSession.sendNotification
import FirebaseSession.sendText
import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.HandlerThread
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.preference.PreferenceManager
import com.example.safepak.data.User
import com.example.safepak.frontend.services.CameraService
import com.example.safepak.frontend.services.LocationService
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.Constants
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.models.notification.NotificationData
import com.example.safepak.logic.models.notification.PushNotification
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.telephony.SmsManager

object EmergencySession {

    private lateinit var myLocation: Location
    private var current_call: Call? = null
    private var people_found = 0

    fun getCurrentCall(context: Context): Call? {
        current_call = LocalDB.getEmergency(context)
        return current_call
    }


    fun initiateLevel1(context: Context?, location: UserLocation) {
        generateCall("level1", context, location)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val send_sim = prefs?.getBoolean("sim_location", false)

        FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}")
            .get().addOnSuccessListener { friend ->
                friend.children.forEach {
                    val friendship = it.getValue(Friendship::class.java)
                    if (friendship?.status == "close") {
                        val get_user = FirebaseFirestore.getInstance().collection("users")
                            .document(friendship.friendid!!)
                        get_user.get().addOnSuccessListener { doc ->
                            val user = doc.toObject(User::class.java)!!
                            sendNotification(
                                PushNotification(
                                    NotificationData(
                                        FirebaseSession.userID!!,
                                        current_call?.id!!,
                                        true,
                                        "level1",
                                        "${user.firstname} here is my location", "Level-1 Alert"
                                    ),
                                    user.registrationTokens.last()
                                )
                            )
                            sendText(
                                user,
                                true,
                                "Hey ${user.firstname}!\nI am feeling a little unsafe so that's why i called for level-1 alert.\nRight now i'm at ${location.address}.\nI am sharing my location in case something inconvenient happens.\nThanks"
                            )
                            if (send_sim == true) {
                                val url = "https://www.google.com/maps/search/?api=1&query=${location.latitude}%2C${location.longitude}"
                                sendSMS(context, user.phone!!, "I'm at ${url}.\nThanks")
                            }
                        }
                    }
                }
            }
    }

    fun initiateLevel2(context: Context?, location: UserLocation) {
        generateCall("level2", context, location)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val send_sim = prefs?.getBoolean("sim_location", true)

        FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}")
            .get().addOnSuccessListener { friend ->
                friend.children.forEach{
                    val friendship = it.getValue(Friendship::class.java)
                    if (friendship?.status == "close") {
                        FirebaseFirestore.getInstance().collection("users").document(it.key!!)
                            .get().addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val user = doc.toObject(User::class.java)!!
                                    sendNotification(
                                        PushNotification(
                                            NotificationData(
                                                FirebaseSession.userID!!,
                                                current_call?.id!!,
                                                true,
                                                "level2",
                                                "Emergency!!! Please Help ${user.firstname}",
                                                "Level-2 Alert",
                                                isclose = true
                                            ),
                                            user.registrationTokens.last()
                                        )
                                    )
                                    sendText(user, true, "Please Help ${user.firstname}!\nIt's an emergency call so that's why this call is generated.\nRight now i'm at ${location.address}.\nI am sharing my location.\nThanks")
                                    if (send_sim == true) {
                                        val url = "https://www.google.com/maps/search/?api=1&query=${location.latitude}%2C${location.longitude}"
                                        sendSMS(context, user.phone!!, "Please Help ${user.firstname}!\nIt's an emergency call so that's why this call is generated.")
                                        sendSMS(context, user.phone!!, "I'm at ${url}.\nHelp")
                                    }
                                }
                            }
                    }
                }
            }

        val switch = prefs?.getBoolean("level2_switch", false)
        val value = prefs.getInt("level2_radius",5)
        val radius_values = arrayOf(2.0, 4.0, value.toDouble())

        if (switch == true) {
            for (i in radius_values) {
                    val center = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
                    searchUserInRadius(center, i)
                }
        }
    }


    fun checkInside(radius : Double, circle : LatLng, position : LatLng) : Boolean {
        val distance = SphericalUtil.computeDistanceBetween(circle, position);

        return distance < radius * 1000
    }


    fun searchUserInRadius(circle_center: LatLng, circle_radius: Double){
        val ref = FirebaseDatabase.getInstance().getReference("/users-location")

        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot: DataSnapshot in dataSnapshot.children) {
                    for (child : DataSnapshot in snapshot.children){

                        val location = child.getValue(UserLocation::class.java)

                        if (location != null && child.key != FirebaseSession.userID) {

                            val start = LatLng(circle_center.latitude, circle_center.longitude)
                            val end = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())

                            if(checkInside(circle_radius, start, end)) {
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(snapshot.key!!).get().addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            val user = doc.toObject(User::class.java)
                                            FirebaseDatabase.getInstance()
                                                .getReference("/friends/${FirebaseSession.userID}/${user!!.userid}/")
                                                .get().addOnSuccessListener { child ->
                                                    var isfriend = false
                                                    var isclose = false
                                                    if (child.exists()) {
                                                        isfriend = true
                                                        val friend = child.getValue(Friendship::class.java)
                                                        if (friend?.status == "close")
                                                            isclose = true
                                                    }
                                                    if (!isclose) {
                                                        sendNotification(
                                                            PushNotification(
                                                                NotificationData(
                                                                    FirebaseSession.userID!!,
                                                                    current_call?.id!!,
                                                                    isfriend,
                                                                    "level2",
                                                                    "Emergency! Please Help This Person",
                                                                    "Level-2 Alert",
                                                                    isclose = false
                                                                ),
                                                                user.registrationTokens.last()
                                                            )
                                                        )
                                                        people_found++
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    private fun sendSMS(context: Context?,phoneNumber: String, message: String) {
        val sentPI: PendingIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"), 0)
        SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, sentPI, null)
    }

    private fun generateCall(type : String, context: Context?, location: UserLocation) {
        val query = FirebaseDatabase.getInstance().getReference("/emergency-calls/${FirebaseSession.userID}/")
        val id = query.push().key
        val call = Call(id, FirebaseSession.userID!!, type, location, getDatetime(),"going")
        current_call = call
        LocalDB.storeEmergency(context!! ,current_call!!)
        query.child(id!!).setValue(call)
    }

    fun stopCall(context: Context?){
        val query = FirebaseDatabase.getInstance()
            .getReference("/emergency-calls/${FirebaseSession.userID}/")
        LocalDB.deleteEmergency(context!!)
        query.child(current_call?.id!!).updateChildren(mapOf("status" to "stopped"))
    }

    @Suppress("DEPRECATION")
    fun Context.isEmergencyServiceRunning(serviceClass: Class<LocationService>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    @Suppress("DEPRECATION")
    fun Context.isCameraServiceRunning(serviceClass: Class<CameraService>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    fun startLocationService(context: Context?,type : String) {
        if (!context?.isEmergencyServiceRunning(LocationService::class.java)!!) {
            val intent = Intent(context, LocationService::class.java)
            intent.action = Constants.ACTION_START_LOCATION_SERVICE
            intent.putExtra("call_type",type)
            context.startService(intent)
            Toast.makeText(context, "Location service started", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopLocationService(context: Context?){
        if(context?.isEmergencyServiceRunning(LocationService::class.java)!!) {
            val intent = Intent(context, LocationService::class.java)
            intent.action = Constants.ACTION_STOP_LOCATION_SERVICE
            context.startService(intent)
            Toast.makeText(context, "Location service stopped", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateUserLocationInFirebase(location: UserLocation?) {
        if (location != null) {
                val location_store = FirebaseDatabase.getInstance().getReference("/users-location/${FirebaseSession.userID!!}/")
                location_store.child(FirebaseSession.userID!!).setValue(location)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDatetime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        val formatted = current.format(formatter)

        return formatted
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    public fun requestLocation(context: Context){
        val mLocationRequest: LocationRequest = LocationRequest.create()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.numUpdates = 1
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
            }
        }
        val handlerThread = HandlerThread("backgroundThread")
        if (!handlerThread.isAlive) handlerThread.start()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, mLocationCallback, handlerThread.looper)
    }

}