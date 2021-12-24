package com.example.safepak.frontend.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityVerifyBinding
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.frontend.login.LoginActivity
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.safepak.logic.implementations.LoginAndRegisterImplementation
import com.example.safepak.logic.interfaces.ILoginAndRegister
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore


class VerifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var codeBySystem: String

    private lateinit var fullname : String
    private lateinit var phone : String
    private lateinit var password : String
    private lateinit var cnic : String
    private lateinit var login_phone : String
    private lateinit var flag : String
    private lateinit var methods : ILoginAndRegister


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        methods = LoginAndRegisterImplementation()

        flag = intent.getStringExtra("flag").toString()


        if (flag.equals("login")) {
            login_phone = intent.getStringExtra("login_phone").toString()
            sendVerificationCodeToUser(login_phone)
        }
        else {
            fullname = intent.getStringExtra("register_name").toString()
            phone = intent.getStringExtra("register_phone").toString()

            password = intent.getStringExtra("register_password").toString()
            cnic = intent.getStringExtra("register_cnic").toString()
            sendVerificationCodeToUser(phone)
        }


        binding.resendText.setOnClickListener{
            Toast.makeText(this@VerifyActivity, "Code resent", Toast.LENGTH_SHORT).show()
            if (flag.equals("login"))
                sendVerificationCodeToUser(login_phone)
            else
                sendVerificationCodeToUser(phone)
        }

        binding.proceedBt.setOnClickListener{
            verifyAndSignIn(binding.verifyBox.text.toString())
        }
    }


    private fun sendVerificationCodeToUser(phoneNo: String) {
        val options = PhoneAuthOptions.newBuilder(auth) //mAuth is defined on top
            .setPhoneNumber("+92${phoneNo.drop(1)}") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                codeBySystem = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    binding.verifyBox.setText(code)
                    verifyAndSignIn(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@VerifyActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    private fun verifyAndSignIn(code: String){
        binding.proceedBt.isEnabled = false
        binding.verifyBar.visibility = View.VISIBLE
        //Verification
        val credential = PhoneAuthProvider.getCredential(codeBySystem, code)

        //Sign-in and creation
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this){ task ->
            if (task.isSuccessful) {
                if(flag.equals("login")){
                    Toast.makeText(this@VerifyActivity, "Logged in", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@VerifyActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    signup(task)
                }
            }
            else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@VerifyActivity, "Invalid OTP Code!", Toast.LENGTH_SHORT).show()
                    binding.proceedBt.isEnabled = true
                    binding.verifyBar.visibility = View.GONE
                }
            }
        }
    }

    private fun signup(task : Task<AuthResult>){
        val user = task.result!!.user
        val creationTimestamp = user!!.metadata!!.creationTimestamp
        val lastSignInTimestamp = user.metadata!!.lastSignInTimestamp
//        if (creationTimestamp == lastSignInTimestamp) {
            val names = fullname.split(" ")
            val adduser = User(user.uid, names[0], names[1], password, cnic, phone,null,null,null,null,null)
            db.collection("users").document(user.uid).set(adduser).addOnSuccessListener {
                Toast.makeText(this@VerifyActivity, "Logged in", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@VerifyActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@VerifyActivity, "Registeration Failed", Toast.LENGTH_SHORT).show()
                binding.proceedBt.isEnabled = true
                binding.verifyBar.visibility = View.GONE
        }
    }


}