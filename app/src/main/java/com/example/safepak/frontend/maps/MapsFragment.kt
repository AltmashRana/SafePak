package com.example.safepak.frontend.maps

import android.content.Context
import android.graphics.Color
import android.location.LocationManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentMapsBinding
import com.example.safepak.logic.models.UserLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_maps.*


class MapsFragment : Fragment() {
    private val PERMISSIONS_FINE_LOCATION = 101

    private var animate_flag = true
    var marker : Marker? = null
    var circle : Circle? = null

    private val mMap: GoogleMap? = null
    private lateinit var user : User
    private var isfriend : Boolean = false
    var previousLatLng: LatLng? = null
    var currentLatLng: LatLng? = null
    private val polyline1: Polyline? = null

    private var place1: MarkerOptions? = null
    private  var place2:MarkerOptions? = null
    private var currentPolyline: Polyline? = null

    private var myLocation : LatLng? = null
    private var polylines: ArrayList<Polyline>? = null
    private val callback = OnMapReadyCallback { googleMap ->

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        val ref = FirebaseDatabase.getInstance().getReference("/users-location/${user.userid}")
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL


        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val location = p0.getValue(UserLocation::class.java)
                if (location != null) {
                    val position = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
                    drawCircle(position, googleMap, 0x200000ff)
                    marker?.remove()
                    marker = googleMap.addMarker(
                        MarkerOptions().position(position).title(user.firstname).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.marker_ic)))!!

                    if (animate_flag) {
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                position, 17.0f
                            )
                        )
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val location = p0.getValue(UserLocation::class.java)
                if (location != null) {
                    val position = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
                    drawCircle(position, googleMap, 0x200000ff)
                    marker?.remove()
                    marker = googleMap.addMarker(
                        MarkerOptions().position(position).title(user.firstname).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.marker_ic)))!!

                    if (animate_flag) {
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                position, 17.0f
                            )
                        )
                    }
                }
            }
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildRemoved(p0: DataSnapshot) {}

            })
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun drawCircle(point: LatLng, googleMap: GoogleMap, color: Int) {

        // Instantiating CircleOptions to draw a circle around the marker
        val circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(point)

        // Radius of the circle
        circleOptions.radius(100.0)

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK)

        // Fill color of the circle
        circleOptions.fillColor(color)

        // Border width of the circle
        circleOptions.strokeWidth(0.5f)

        // Adding the circle to the GoogleMap
        circle?.remove()
        circle = googleMap.addCircle(circleOptions)
    }

    private val DEFAULT_UPDATE_INTERVAL = 10
    private val FAST_UPDATE_INTERVAL = 5

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null


    override fun onStop() {
        super.onStop()
//        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
//        stopLocationUpdates()
    }

    lateinit var binding: FragmentMapsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        val bundle = this.arguments
        user = bundle?.getParcelable<User>("user") as User
        isfriend = bundle.getBoolean("is_friend", false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        updateGPS()

        locationRequest = LocationRequest()
        locationRequest!!.interval = (1000 * DEFAULT_UPDATE_INTERVAL).toLong()
        locationRequest!!.fastestInterval = (1000 * FAST_UPDATE_INTERVAL).toLong()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //this is triggered when update location interval is met (in our case, it is 30 seconds)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                myLocation = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                Toast.makeText(view.context, "Location updated", Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.blood_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        mapsAnimate_bt.setOnClickListener {
            animate_flag = !animate_flag
            Toast.makeText(view.context, "Animation changed", Toast.LENGTH_SHORT).show()
        }
    }

}