package com.example.safepak.frontend.register

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.safepak.databinding.ActivityRegisterBinding
import com.example.safepak.frontend.login.LoginActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.firebase.firestore.FirebaseFirestore

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

class RegisterActivity : AppCompatActivity(){

    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "RegisterActivity"

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        MobileAds.initialize(this@RegisterActivity)

            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.show(this@RegisterActivity)
                }
            })

            mInterstitialAd?.show(this)

            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(TAG, "Ad failed to show.")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    mInterstitialAd = null
                }
            }


        db = FirebaseFirestore.getInstance()

        binding.registeredBt.setOnClickListener {

            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.submitBt.setOnClickListener {
            val techniques = Techniques.Shake
            val duration : Long = 400
            var isValid = true

            val name = binding.fullnameText.editText?.text.toString()
            val phone = binding.phoneText.editText?.text.toString()
            val password = binding.passwordText.editText?.text.toString()
            val cpassword = binding.cpasswordText.editText?.text.toString()
            val cnic = binding.cnicText.editText?.text.toString()

            val namePattern = "[a-zA-Z0-9.-]+ [a-zA-Z0-9.-]+"
            if (name.let { it.isEmpty() or (!it.matches(namePattern.toRegex()))}) {
                binding.fullnameText.error = "Enter a full name"
                isValid = false
                YoYo.with(techniques)
                    .duration(duration)
                    .repeat(1)
                    .playOn(binding.fullnameText)
            } else {
                binding.fullnameText.error = null
                binding.fullnameText.isErrorEnabled = false
            }
            if (phone.length != 11) {
                binding.phoneText.error = "Enter a valid phone number"
                isValid = false
                YoYo.with(techniques)
                    .duration(duration)
                    .repeat(1)
                    .playOn(binding.phoneText)
            } else {
                binding.phoneText.error = null
                binding.phoneText.isErrorEnabled = false

            }
            if (password.let { it.isEmpty() or (it.length < 8) }) {
                binding.passwordText.error = "Enter a 8-digit password"
                isValid = false
                YoYo.with(techniques)
                    .duration(duration)
                    .repeat(1)
                    .playOn(binding.passwordText)
            } else {
                binding.passwordText.error = null
                binding.passwordText.isErrorEnabled = false
            }
            if (cpassword != password || cpassword.isEmpty()) {
                binding.cpasswordText.error = "password does not match"
                isValid = false
                YoYo.with(techniques)
                    .duration(duration)
                    .repeat(1)
                    .playOn(binding.cpasswordText)
            } else {
                binding.cpasswordText.error = null
                binding.cpasswordText.isErrorEnabled = false
            }
            if (cnic.length != 13) {
                binding.cnicText.error = "Enter a valid cnic"
                isValid = false
                YoYo.with(techniques)
                    .duration(duration)
                    .repeat(1)
                    .playOn(binding.cnicText)
            } else {
                binding.cnicText.error = null
                binding.cnicText.isErrorEnabled = false
            }

            if (isValid) {
                val query = db.collection("users").whereEqualTo("phone", phone)
                query.get().addOnSuccessListener { doc ->
                    if (doc.documents.size == 0) {
                        val intent = Intent(this@RegisterActivity, VerifyActivity::class.java)
                        intent.putExtra("flag", "register")
                        intent.putExtra("register_name", name)
                        intent.putExtra("register_phone", phone)
                        intent.putExtra("register_password", password)
                        intent.putExtra("register_cnic", cnic)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@RegisterActivity, "User Already Exist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}