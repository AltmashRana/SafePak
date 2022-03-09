package com.example.safepak.frontend.home

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.example.safepak.frontend.services.GestureService
import com.example.safepak.frontend.services.LocationService
import com.google.firebase.auth.FirebaseAuth

class SafePak : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        mySettings()
    }

    private fun mySettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val switch = prefs?.getBoolean("gesture_switch", false)

        if (switch == true) {
            val intent = Intent(this, GestureService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            }
            else
                startService(intent)
        } else {
            val intent = Intent(this, GestureService::class.java)
            stopService(intent)
        }


        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {
            //App in background
            if (FirebaseAuth.getInstance().currentUser != null)
                FirebaseSession.updateUserStatus("offline")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onAppForegrounded() {
            // App in foreground
            if (FirebaseAuth.getInstance().currentUser != null)
                FirebaseSession.updateUserStatus("online")
        }
    }
}