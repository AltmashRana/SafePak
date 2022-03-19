package com.example.safepak.frontend.announcement

import android.Manifest
import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.databinding.ActivityRedzoneBinding
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.session.EmergencySession
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.lang.Exception
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import java.util.*
import com.google.android.libraries.places.api.Places
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RedzoneActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityRedzoneBinding

    private lateinit var mylocation : LatLng
    private lateinit var mMap: GoogleMap
    private var circle: Circle? = null
    var count = 0

    lateinit var valueAnimator: ValueAnimator
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRedzoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val colorDrawable = ColorDrawable(Color.RED)
        supportActionBar!!.setBackgroundDrawable(colorDrawable)

        updateGPS()

        val apiKey = getString(R.string.google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        val placesClient = Places.createClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (place.latLng != null) {
                    count = 0
                    mylocation = place.latLng!!
                    findEmergencyCalls()
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            place.latLng!!, 14.0f
                        )
                    )
                }
            }
            override fun onError(status: Status) {
                Toast.makeText(this@RedzoneActivity, "Error in search", Toast.LENGTH_SHORT).show()
            }
        })

        findEmergencyCalls()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                mylocation, 14.0f
            )
        )

        findEmergencyCalls()
    }

    private fun findEmergencyCalls() {
        val ref = FirebaseDatabase.getInstance().getReference("/emergency-calls")

        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val center = LatLng(mylocation.latitude!!.toDouble(), mylocation.longitude!!.toDouble())
                for (snapshot: DataSnapshot in dataSnapshot.children){
                    for (child: DataSnapshot in snapshot.children) {
                        child.getValue(Call::class.java)?.let {
                            if (it.location != null) {
                                val call_loc = LatLng(it.location?.latitude!!.toDouble(), it.location?.longitude!!.toDouble())

                                val calltime = it.time!!.split(',')[0].split('/')
                                val time = EmergencySession.getDatetime().split(',')[0].split('/')
                                if (EmergencySession.checkInside(10.0, center, call_loc)) {
                                    if(time[0] == calltime[0] && time[2] == calltime[2])
                                        count++
                                }
                            }
                        }
                    }
                }
               if (count == 0) {
                   binding.redzoneText.text = "Safe zone"
                   binding.redzoneText.setTextColor(Color.GRAY)
               }
                else {
                   binding.redzoneText.text = "$count Emergency calls here!"
                   binding.redzoneText.setTextColor(Color.RED)
                   animateCircle(1600f, center)
                }
            }
        }
        ref.addListenerForSingleValueEvent(listener)
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
                try {
                    mylocation = LatLng(location.latitude, location.longitude)
                        val mapFragment = supportFragmentManager.findFragmentById(R.id.red_map) as? SupportMapFragment
                        mapFragment?.getMapAsync(this)
                } catch (e: Exception) {
                    Toast.makeText(this, "Address capture failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun animateCircle(radius : Float, position : LatLng){
        Handler().postDelayed({
            val groundOverlay11= mMap.addGroundOverlay(
                GroundOverlayOptions()
                    .position(position, radius)
                    .transparency(0.8f)
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.circle))
            )
            OverLay(groundOverlay11!!,radius)
        }, 0)
        Handler().postDelayed({
            val groundOverlay1 = mMap.addGroundOverlay(
                GroundOverlayOptions()
                    .position(position, radius)
                    .transparency(0.8f)
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.circle))
            )
            OverLay(groundOverlay1!!,radius)
        }, 3000)

        Handler().postDelayed({
            val groundOverlay2 = mMap.addGroundOverlay(
                GroundOverlayOptions()
                    .position(position, radius)
                    .transparency(0.8f)
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
        valueAnimator.addUpdateListener { valueAnimator ->
            val animatedFraction = valueAnimator.animatedFraction
            val i = valueAnimator.animatedValue as Int
            groundOverlay.setDimensions(i.toFloat())
        }
        valueAnimator.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}