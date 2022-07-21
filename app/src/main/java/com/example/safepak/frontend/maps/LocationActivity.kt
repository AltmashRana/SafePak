package com.example.safepak.frontend.maps

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.example.safepak.R
import com.example.safepak.databinding.ActivityLocationBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.safepak.data.User
import com.example.safepak.frontend.chat.ChatBoxActivity
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.UserLocation
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var db: FirebaseFirestore
    private var directionsActivityCode = 911

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        var color = ContextCompat.getDrawable(this, R.drawable.chatlist_bg)
//        supportActionBar?.setBackgroundDrawable(color);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        if (!isLocationEnabled(this)) {
            Toast.makeText(view.context, "Please Enable location", Toast.LENGTH_SHORT).show()
            finish()
        }

        val bundle = Bundle()
        val user = intent.extras?.get("user") as User
        val callid = intent.getStringExtra("call_id")
        val isfriend = intent.getBooleanExtra("is_friend", false)
        bundle.putParcelable("user", user)
        bundle.putBoolean("is_friend", isfriend)
        val fragInfo = MapsFragment()
        fragInfo.arguments = bundle

        if (!isfriend) {
            binding.locationchatBt.visibility = View.GONE
            binding.locationchatBt.text = "Chat with ${user.firstname}"
        }

        checkCallStatus(callid!!, user.userid!!)

        binding.locationchatBt.setOnClickListener{
            val intent = Intent(this@LocationActivity, ChatBoxActivity::class.java)
            intent.putExtra("USER_KEY", user)
            intent.putExtra("IS_FRIEND", isfriend)
            startActivity(intent)
            finish()
        }


        binding.directionsBt.setOnClickListener{
            val intent = Intent(this@LocationActivity, DirectionsActivity::class.java)
            intent.putExtra("USER_KEY", user)
            startActivityForResult(intent, directionsActivityCode)
            finish()
        }

        loadFragment(fragInfo)
    }

//    private fun checkIsFriend(user: User) {
//        FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}/${user.userid}")
//            .get().addOnSuccessListener { doc ->
//                if (doc.exists()){
//                 binding.locationchatBt.isEnabled = true
//                 binding.locationchatBt.text = user.firstname
//                }
//            }
//    }

    private fun checkCallStatus(callid : String, userid : String){
        FirebaseDatabase.getInstance().getReference("/emergency-calls/$userid/$callid")
        .addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if (p0.value == "stopped"){
                    Toast.makeText(this@LocationActivity, "Session Expired!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LocationActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.value == "stopped"){
                    Toast.makeText(this@LocationActivity, "Session Expired!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LocationActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.mapsFrame.id, fragment)
            .commit();
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}