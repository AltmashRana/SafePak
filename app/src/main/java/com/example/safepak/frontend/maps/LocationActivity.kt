package com.example.safepak.frontend.maps

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.example.safepak.databinding.ActivityLocationBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.safepak.data.User
import com.example.safepak.frontend.chat.ChatBoxActivity


class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var db: FirebaseFirestore

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
            Toast.makeText(view.context, "Please Enable location}", Toast.LENGTH_SHORT).show()
            finish()
        }

        val bundle = Bundle()
        val user = intent.extras?.get("user") as User
        bundle.putParcelable("user", user)
        val fragInfo = MapsFragment()
        fragInfo.arguments = bundle

        binding.locationchatBt.text = "Chat with ${user.firstname}"

        binding.locationchatBt.setOnClickListener{
            val intent = Intent(this@LocationActivity, ChatBoxActivity::class.java)
            intent.putExtra("USER_KEY", user)
            intent.putExtra("IS_FRIEND", true)
            startActivity(intent)
            finish()
        }


        binding.directionsBt.setOnClickListener{
            val intent = Intent(this@LocationActivity, DirectionsActivity::class.java)
            intent.putExtra("USER_KEY", user)
            startActivity(intent)
            finish()
        }

        loadFragment(fragInfo)
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