package com.example.safepak.logic.session

import FirebaseSession.sendNotification
import FirebaseSession.sendText
import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.safepak.data.User
import com.example.safepak.frontend.services.LocationService
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.Constants
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.models.notification.NotificationData
import com.example.safepak.logic.models.notification.PushNotification
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


object EmergencySession {

    private lateinit var myLocation : Location
    private var current_call : Call? = null

    fun getCurrentCall(context : Context) : Call? {
        current_call = LocalDB.getEmergency(context)
        return current_call
    }


    fun initiateLevel1(context: Context?, location: UserLocation) {
        generateCall("level1",context, location)

        FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}")
            .get().addOnSuccessListener { friend ->
                friend.children.forEach {
                    val friendship = it.getValue(Friendship::class.java)
                    if (friendship?.status == "close") {
                        val get_user = FirebaseFirestore.getInstance().collection("users")
                            .document(friendship.friendid!!)
                        get_user.get().addOnSuccessListener { doc ->
                            val user = doc.toObject(User::class.java)!!
                            sendNotification(PushNotification(NotificationData(FirebaseSession.userID!!, "level1","${user.firstname} here is my location","Level-1 Alert"),user.registrationTokens.last()))
                            sendText(user, "Hey ${user.firstname}!\nI am feeling a little unsafe so that's why i called for level-1 alert.\nRight now i'm at ${location.address}.\nI am sharing my location in case something inconvenient happens.\nThanks")
                        }
                    }
                }
            }
    }
    fun initiateLevel2(context: Context?, location: UserLocation) {
        generateCall("level2",context, location)

        FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}")
            .get().addOnSuccessListener { friend ->
                friend.children.forEach {
                    val friendship = it.getValue(Friendship::class.java)
                    if (friendship?.status == "close") {
                        val get_user = FirebaseFirestore.getInstance().collection("users")
                            .document(friendship.friendid!!)
                            get_user.get().addOnSuccessListener { doc ->
                            val user = doc.toObject(User::class.java)!!
                            sendNotification(PushNotification(NotificationData(FirebaseSession.userID!!, "level2", "Emergency!!! Please Help ${user.firstname}", "Level-2 Alert"), user.registrationTokens.last()))
                            sendText(user, "PLEASE HELP ${user.firstname}!\nIt's an emergency call so that's why i called for level-2 alert.\nRight now i'm at ${location.address}.\nI am sharing my location live location.\nThanks")
                        }
                    }
                }
            }
    }

    private fun generateCall(type : String, context: Context?, location: UserLocation) {
        val query = FirebaseDatabase.getInstance().getReference("/emergency-calls/${FirebaseSession.userID}/")
        val id = query.push().key
        val call = Call(id, FirebaseSession.userID!!, type, location.address, getDatetime(),"going")
        current_call = call
        LocalDB.storeEmergency(context!! ,current_call!!)
        query.child(id!!).setValue(call)
    }

    fun stopCall(context: Context?){
        val query = FirebaseDatabase.getInstance()
            .getReference("/emergency-calls/${FirebaseSession.userID}/")
        LocalDB.deleteEmergency(context!!,current_call!!.id!!)
        query.child(current_call?.id!!).updateChildren(mapOf("status" to "stopped"))
    }

    @Suppress("DEPRECATION")
    fun Context.isMyServiceRunning(serviceClass: Class<LocationService>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

    fun startLocationService(context: Context?) {
        if (!context?.isMyServiceRunning(LocationService::class.java)!!) {
            val intent = Intent(context, LocationService::class.java)
            intent.action = Constants.ACTION_START_LOCATION_SERVICE
            context.startService(intent)
            Toast.makeText(context, "Location service started", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopLocationService(context: Context?){
        if(context?.isMyServiceRunning(LocationService::class.java)!!) {
            val intent = Intent(context, LocationService::class.java)
            intent.action = Constants.ACTION_STOP_LOCATION_SERVICE
            context.startService(intent)
            Toast.makeText(context, "Location service stopped", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateUserLocationInFirebase(context : Context, location: Location?) {
        //this method will originally update user location in firebase database
        //for now, it is updating only UI components for testing purposes
        if (location != null) {
            val geocoder = Geocoder(context)
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val loc = UserLocation(
                    location.longitude.toString(),
                    location.latitude.toString(),
                    addresses[0].getAddressLine(0).toString()
                )
                val location_store = FirebaseDatabase.getInstance()
                    .getReference("/users-location/${FirebaseSession.userID!!}/")
                location_store.child(FirebaseSession.userID!!).setValue(loc)

            } catch (e: java.lang.Exception) {
                Toast.makeText(context, "Address capture failed", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDatetime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        val formatted = current.format(formatter)

        return formatted
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}