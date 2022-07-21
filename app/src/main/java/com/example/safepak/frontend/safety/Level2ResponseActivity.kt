package com.example.safepak.frontend.safety

import android.Manifest
import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityLevel2ResponseBinding
import com.example.safepak.frontend.maps.LocationActivity
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.session.EmergencySession.getDatetime
import com.example.safepak.logic.session.EmergencySession.requestLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

import android.view.animation.LinearInterpolator

import com.google.android.gms.maps.model.GroundOverlay
import android.os.Handler
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.logic.session.EmergencySession
import com.example.safepak.logic.session.LocalDB

import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class Level2ResponseActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityLevel2ResponseBinding

    private lateinit var mylocation : UserLocation
    private lateinit var mMap: GoogleMap
    private var circle: Circle? = null

    lateinit var valueAnimator: ValueAnimator
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLevel2ResponseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = intent.extras?.get("user") as User
        val callid = intent.getStringExtra("call_id")
        val isfriend = intent.getBooleanExtra("is_friend", false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        checkCallStatus(callid!!, user.userid!!)

        if (isfriend)
            binding.level2helpText.text = "${user.firstname} is in Danger"
        else
            binding.level2helpText.text = "A Person is in Danger"

        requestLocation(this)
        updateGPS()

        binding.level2responseBt.setOnClickListener {
            val intent = Intent(this@Level2ResponseActivity, LocationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("call_id", callid)
            intent.putExtra("is_friend", isfriend)
            FirebaseDatabase.getInstance().getReference("/emergency-calls/${user.userid}/${callid}")
                .updateChildren(mapOf(
                    "respondentid" to FirebaseSession.userID,
                    "responsetime" to getDatetime(),
                    "respondentlocation" to mylocation
                )).addOnSuccessListener {
                    LocalDB.storeResponses(this, callid!!, user.userid!!)
                    Toast.makeText(this, "Responded", Toast.LENGTH_SHORT).show()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
        }

        binding.leve2cancelBt.setOnClickListener {
            finishAndRemoveTask()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val position = LatLng(mylocation.latitude!!.toDouble(), mylocation.longitude!!.toDouble())
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position, 14.0f
            )
        )
//        drawCircle(position, mMap, 500.0, 0x20ff0000)
        animateCircle(1400f, position)
    }


    private fun checkCallStatus(callid : String, userid : String){
        FirebaseDatabase.getInstance().getReference("/emergency-calls/$userid/$callid")
            .addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    if (p0.value == "stopped"){
                        Toast.makeText(this@Level2ResponseActivity, "Session Expired!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Level2ResponseActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    if (p0.value == "stopped"){
                        Toast.makeText(this@Level2ResponseActivity, "Session Expired!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Level2ResponseActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                }
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildRemoved(p0: DataSnapshot) {}

            })
    }

    private fun updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //we have permission from user
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                val geocoder = Geocoder(this)
                try {
                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0].getAddressLine(0).toString()
                    mylocation = UserLocation(location.longitude.toString(), location.latitude.toString(), address)
                    binding.level2addressText.text = mylocation.address?.substringBeforeLast(',')
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.level2response_map) as? SupportMapFragment
                    mapFragment?.getMapAsync(this)

                } catch (e: Exception) {
                    Toast.makeText(this, "Address capture failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun animateCircle(radius : Float, position : LatLng){
        Handler().postDelayed(Runnable {
            val groundOverlay11= mMap.addGroundOverlay(
                GroundOverlayOptions()
                    .position(position, radius)
                    .transparency(0.7f)
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.circle))
            )
            OverLay(groundOverlay11!!,radius)
        }, 0)
        Handler().postDelayed(Runnable {
            val groundOverlay1 = mMap.addGroundOverlay(
                GroundOverlayOptions()
                    .position(position, radius)
                    .transparency(0.7f)
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.circle))
            )
            OverLay(groundOverlay1!!,radius)
        }, 3000)

        Handler().postDelayed(Runnable {
            val groundOverlay2 = mMap.addGroundOverlay(
                GroundOverlayOptions()
                    .position(position, radius)
                    .transparency(0.7f)
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.circle))
            )
            OverLay(groundOverlay2!!, radius)
        }, 7000)}

    fun OverLay(groundOverlay: GroundOverlay, radius: Float) {
        valueAnimator = ValueAnimator.ofInt(0, radius.toInt())
        val r = 99999
        valueAnimator.repeatCount = r
        //vAnimator.setIntValues(0, 500);
        valueAnimator.duration = 12000
        valueAnimator.setEvaluator(IntEvaluator())
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            val animatedFraction = valueAnimator.animatedFraction
            val i = valueAnimator.animatedValue as Int
            groundOverlay.setDimensions(i.toFloat())
        })
        valueAnimator.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}