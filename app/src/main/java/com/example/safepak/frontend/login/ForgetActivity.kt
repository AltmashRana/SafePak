package com.example.safepak.frontend.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safepak.databinding.ActivityForgetBinding

class ForgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}