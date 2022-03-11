package com.example.safepak.frontend.blood


import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityBloodResponseBinding
import com.example.safepak.logic.models.UserLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.drawable.ColorDrawable


class BloodResponseActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var location : UserLocation
    private lateinit var user : User
    private lateinit var blood : String

    private lateinit var binding: ActivityBloodResponseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBloodResponseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        location = intent?.getParcelableExtra("LOCATION")!!
        user = intent?.getParcelableExtra("USER")!!
        blood = intent?.getStringExtra("BLOOD")!!

        val colorDrawable = ColorDrawable(Color.RED)
        supportActionBar!!.setBackgroundDrawable(colorDrawable)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "${user.firstname}'s Broadcast"
        supportActionBar?.setDisplayShowHomeEnabled(true)


        binding.bloodaddressText.text = location.address

        binding.bloodgroupText.text = "$blood Blood Required"


        val mapFragment = supportFragmentManager.findFragmentById(R.id.response_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.bloodresponseBt.setOnClickListener {
            FirebaseSession.sendText(user, "\uD83E\uDE78Hi, ${user.firstname}. I think i can arrange $blood blood\uD83E\uDE78")
            Toast.makeText(this, "Responded", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.bloodcancelBt.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val position = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
        mMap.addMarker(MarkerOptions().position(position).title("${user.firstname}"))
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position, 17.0f
            )
        )
        drawCircle(position, mMap, 100.0,0x20ff0000)

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

        // Adding the circle to the GoogleMap
        googleMap.addCircle(circleOptions)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}