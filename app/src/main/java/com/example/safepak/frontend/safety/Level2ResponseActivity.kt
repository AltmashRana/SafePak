package com.example.safepak.frontend.safety

import android.Manifest
import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.databinding.ActivityLevel2ResponseBinding
import com.example.safepak.logic.session.EmergencySession.requestLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import java.lang.Exception

class Level2ResponseActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityLevel2ResponseBinding

    private lateinit var mylocation : Location
    private lateinit var mMap: GoogleMap
    private var circle: Circle? = null

    val valueAnimator = ValueAnimator()
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLevel2ResponseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        requestLocation(this)
        updateGPS()

        binding.level2responseBt.setOnClickListener {

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val position = LatLng(mylocation.latitude, mylocation.longitude)
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position, 17.0f
            )
        )

        drawCircle(position, mMap, 100.0, 0x20ff0000)

        animateCircle(100.0)
    }

    private fun drawCircle(point: LatLng, googleMap: GoogleMap, radius: Double, color: Int) {

        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(point)

        // Radius of the circle
        circleOptions.radius(radius)

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK)

        // Fill color of the circle
        circleOptions.fillColor(color)

        // Border width of the circle
        circleOptions.strokeWidth(0.5f)

        circle?.remove()

        // Adding the circle to the GoogleMap
        circle = googleMap.addCircle(circleOptions)
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
                    mylocation = location
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.level2response_map) as? SupportMapFragment
                    mapFragment?.getMapAsync(this)

                } catch (e: Exception) {
                    Toast.makeText(this, "Address capture failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun animateCircle(radius : Double){

        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.setIntValues(0, 100)
        valueAnimator.duration = 3000
        valueAnimator.setEvaluator(IntEvaluator())
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            val animatedFraction = valueAnimator.animatedFraction
            circle?.radius = (animatedFraction * radius)
        }
        valueAnimator.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}