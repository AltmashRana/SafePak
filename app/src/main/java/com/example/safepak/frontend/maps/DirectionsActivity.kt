package com.example.safepak.frontend.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityDirectionsBinding
import com.example.safepak.logic.models.UserLocation
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_directions.*

class DirectionsActivity : AppCompatActivity(), OnMapReadyCallback, TaskLoadedCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDirectionsBinding
    private var animate_flag = true
    private var marker : Marker? = null
    private var circle : Circle? = null

    private var place1: MarkerOptions? = null
    private  var place2:MarkerOptions? = null

    private lateinit var start : LatLng
    private lateinit var end : LatLng

    private var currentPolyline: Polyline? = null

    private val DEFAULT_UPDATE_INTERVAL = 10
    private val FAST_UPDATE_INTERVAL = 5

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private val PERMISSIONS_FINE_LOCATION = 101
    private lateinit var user : User

    private lateinit var context : Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityDirectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        user = intent.extras?.get("USER_KEY") as User

        context = this

        updateGPS()

        directionsAnimate_bt.setOnClickListener {
            animate_flag = !animate_flag
            Toast.makeText(applicationContext, "Animation Changed", Toast.LENGTH_SHORT).show()
        }

        locationRequest = LocationRequest()
        locationRequest!!.interval = (1000 * DEFAULT_UPDATE_INTERVAL).toLong()
        locationRequest!!.fastestInterval = (1000 * FAST_UPDATE_INTERVAL).toLong()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //this is triggered when update location interval is met (in our case, it is 30 seconds)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                start = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                if (animate_flag)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 17.0f))
                Toast.makeText(applicationContext, "Location updated", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        startLocationUpdates()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            googleMap.isMyLocationEnabled = true

        val ref = FirebaseDatabase.getInstance().getReference("/users-location/${user.userid}")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val location = p0.getValue(UserLocation::class.java)
                if (location != null) {
                    end = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
                    place2 = MarkerOptions().position(LatLng(end.latitude, end.longitude)).title(user.firstname).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.marker_ic))

                    fetchURL(context).execute(
                        getUrl(
                            start,
                            place2!!.position,
                            "driving"
                        ), "driving"
                    )
                    marker?.remove()
                    marker = googleMap.addMarker(place2!!)

                    binding.directionsAddress.text = location.address?.substringBeforeLast(',')

                    drawCircle(place2!!.position, mMap, 0x200000ff)
                }
            }
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val location = p0.getValue(UserLocation::class.java)
                if (location != null) {
                    end = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
                    place2 = MarkerOptions().position(LatLng(end.latitude, end.longitude)).title(user.firstname).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.marker_ic))

                    fetchURL(context).execute(
                        getUrl(
                            start,
                            place2!!.position,
                            "driving"
                        ), "driving"
                    )

                    marker?.remove()
                    marker = googleMap.addMarker(place2!!)

                    binding.directionsAddress.text = location.address

                    drawCircle(place2!!.position, mMap, 0x200000ff)
                }
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }

    override fun onTaskDone(vararg values: Any?) {
        if (currentPolyline != null) currentPolyline!!.remove()
        currentPolyline = mMap.addPolyline((values[0] as PolylineOptions?)!!)
    }

    private fun getUrl(origin: LatLng, dest: LatLng, directionMode: String): String {
        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Mode
        val mode = "mode=$directionMode"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=" + getString(
            R.string.google_maps_key
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun updateGPS() {
        //get permission from user for location access
        //get current location
        //update location on UI (originally in firebase)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //we do not have permission from user - ask for permission
        //Check if our android device have location services and request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //we have permission from user
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener{ location ->
                start = LatLng(location.latitude, location.longitude)
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.blood_map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        } else
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_FINE_LOCATION
            )
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                Looper.myLooper()!!
            )
        }
    }

    private fun stopLocationUpdates() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
    }

    private fun drawCircle(point: LatLng, googleMap: GoogleMap, color: Int) {

        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(point)

        // Radius of the circle
        circleOptions.radius(200.0)

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

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
     stopLocationUpdates()
    }
}