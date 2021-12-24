package com.example.safepak.frontend.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.safepak.databinding.ActivityHomeBinding
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.frontend.announcement.AnnouncementsFragment
import com.example.safepak.frontend.chat.ChatsFragment


import com.example.safepak.frontend.login.LoginActivity
import com.example.safepak.frontend.other.ProfileActivity
import com.example.safepak.frontend.other.SettingsActivity
import com.example.safepak.frontend.safety.SafetyFragment
import com.example.safepak.frontend.status.StatusFragment
import com.example.safepak.logic.session.StorageSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    lateinit var auth: FirebaseAuth


    override fun onResume() {
        super.onResume()
        FirebaseSession.getCurrentUser { user ->

            Glide.with(this)
                .load(user.img?.let { StorageSession.pathToReference(it) })
                .placeholder(R.drawable.empty_dp)
                .into(binding.homeDp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        //set default fragment
        loadFragment(ChatsFragment())

        binding.bottomNavigation.itemIconTintList = null

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.chat_menu -> {
                    loadFragment(ChatsFragment())
                }
                R.id.story_menu -> {
                    loadFragment(StatusFragment())
                }
                R.id.safety_menu -> {
                    loadFragment(SafetyFragment())
                }
                R.id.announcement_menu -> {
                    loadFragment(AnnouncementsFragment())
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.homeDp.setOnClickListener{
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }



//        binding.homeFrame.setOnTouchListener(object : OnSwipeListener(view.context)
//        {
//            override fun onSwipeTop() {
//                Toast.makeText(view.context, "top", Toast.LENGTH_SHORT).show();
//            }
//            override fun onSwipeRight() {
//
//            // todo function to change fragments something like
//            getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(
//                    R.anim.nav_default_enter_anim,  // enter
//                    R.anim.slide_out // exit
//                )
//                .replace(R.id.fragmentContainer,
//                    selectedFragment).commit();
//
//                Toast.makeText(view.context, "right", Toast.LENGTH_SHORT).show();
//            }
//            override fun onSwipeLeft() {
//                Toast.makeText(view.context, "left", Toast.LENGTH_SHORT).show();
//            }
//            override fun onSwipeBottom() {
//                Toast.makeText(view.context, "bottom", Toast.LENGTH_SHORT).show();
//            }
//
//        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.home_frame, fragment)
            .commit();
    }
    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        menuInflater.inflate(R.menu.main_dropdown, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.faces_menu -> {
                Toast.makeText(this, "Saved Faces", Toast.LENGTH_SHORT).show()
            }
            R.id.videos_menu -> {
                Toast.makeText(this, "Videos", Toast.LENGTH_SHORT).show()
            }
            R.id.logout_menu -> {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                auth = Firebase.auth
                auth.signOut()
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.about_menu -> {
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
            }
            R.id.settings_menu -> {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@HomeActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}