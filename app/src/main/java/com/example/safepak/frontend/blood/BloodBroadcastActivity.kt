package com.example.safepak.frontend.blood

import android.Manifest
import android.R.attr
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.safepak.R
import com.example.safepak.databinding.ActivityBloodBroadcastBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception
import android.R.attr.radius

import android.animation.ValueAnimator

import android.view.animation.AccelerateDecelerateInterpolator

import android.animation.IntEvaluator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.safepak.logic.models.UserLocation
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import kotlin.math.ceil
import kotlin.math.log2


class BloodBroadcastActivity : AppCompatActivity(), OnMapReadyCallback , IBloodBroadcast {

    private lateinit var mylocation : Location
    private lateinit var mMap: GoogleMap
    private var circle: Circle? = null
    private lateinit var binding: ActivityBloodBroadcastBinding

    val valueAnimator = ValueAnimator()
    var address = ""
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBloodBroadcastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val colorDrawable = ColorDrawable(Color.RED)
        supportActionBar!!.setBackgroundDrawable(colorDrawable)

        updateGPS()
        loadFragment(RequestFragment())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val position = LatLng(mylocation.latitude, mylocation.longitude)
        mMap.addMarker(MarkerOptions().position(position).title("You"))
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
               position, 17.0f
            )
        )
        drawCircle(position, mMap, 100.0,0x20ff0000)

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.blood_frame, fragment)
            .commit();
    }

    private fun drawCircle(point: LatLng, googleMap: GoogleMap, radius : Double, color: Int) {

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

    private fun updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //we have permission from user
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener{ location ->
                val geocoder = Geocoder(this)
                try {
                    mylocation = location
                        address = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )[0].getAddressLine(0).toString()
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.blood_map) as? SupportMapFragment
                    mapFragment?.getMapAsync(this)
                } catch (e: Exception) {
                    Toast.makeText(this, "Address capture failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun generateBroadcast(blood: String, radius : Double) {
        val bundle = Bundle()
        bundle.putString("BLOOD", blood)

        val values = calculateCircleRadiusAndZoom(radius)
        drawCircle(LatLng(mylocation.latitude, mylocation.longitude), mMap, values.first, 0x20ff0000)
        animateCircle(values.first)

        val circle_center = UserLocation( mylocation.longitude.toString(), mylocation.latitude.toString(), address)

        bundle.putParcelable("CIRCLE", circle_center)
        bundle.putDouble("RADIUS", radius)


        val transaction = this.supportFragmentManager.beginTransaction()
        val fragment = BroadcastFragment()
        fragment.arguments = bundle
            transaction.replace(R.id.blood_frame, fragment)
                .commit()
        }

    override fun stopBroadcast() {
        loadFragment(RequestFragment())
        circle?.remove()
        valueAnimator.cancel()
    }

    override fun changeCircle(radius: Double) {

        val position = LatLng(mylocation.latitude, mylocation.longitude)

        val values = calculateCircleRadiusAndZoom(radius)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, values.second.toFloat()))

        drawCircle(LatLng(mylocation.latitude, mylocation.longitude), mMap, values.first, 0x20ff0000)
    }

    fun calculateCircleRadiusAndZoom(radius: Double) : Pair<Double, Double>{
        var zoom = 17.0
        zoom -= 6.5 * radius / zoom

        var circle_radius = 100.0
        when {
            radius < 14 -> circle_radius += (radius * radius/zoom) * 300
            radius > 14 -> circle_radius += (radius) * 600
        }

        return Pair(circle_radius, zoom)
    }
}