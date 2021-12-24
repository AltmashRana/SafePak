package com.example.safepak.frontend.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityLoginBinding
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.frontend.register.RegisterActivity
import com.example.safepak.frontend.register.VerifyActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        binding.notregisteredBt.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginBt.setOnClickListener{
            val phone = binding.phoneText.editText?.text.toString()
            val password = binding.passwordText.editText?.text.toString()
            if(password.length < 8){
                binding.passwordText.error = "Enter atleast 8-digit password"
            } else{
                binding.passwordText.error = null
                binding.passwordText.isErrorEnabled = false
            }
            if(phone.length != 11){
                binding.phoneText.error = "Enter a valid phone number"
                return@setOnClickListener
            } else{
                binding.phoneText.error = null
                binding.phoneText.isErrorEnabled = false
            }
            signin()
        }

        binding.forgetText.setOnClickListener{
            val intent = Intent(this@LoginActivity, ForgetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signin() {
        binding.loginBar.visibility = View.VISIBLE
        binding.loginBt.isEnabled = false
        val phone = binding.phoneText.editText?.text.toString()
        val password = binding.passwordText.editText?.text.toString()
        val query = db.collection("users").whereEqualTo("phone", phone)
        query.get().addOnSuccessListener {doc ->
            if(doc.documents.size > 0) {
                val stored_password = doc.documents[0].data?.get("password")
                if (stored_password == password) {

                    val intent = Intent(this@LoginActivity, VerifyActivity::class.java)
                    intent.putExtra("flag", "login")
                    intent.putExtra("login_phone", phone)
                    startActivity(intent)
                    finish()
                } else {
                    binding.loginBar.visibility = View.GONE
                    binding.loginBt.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Incorrect Password", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this@LoginActivity, "User Does Not Exist", Toast.LENGTH_SHORT).show()
                binding.loginBar.visibility = View.GONE
                binding.loginBt.isEnabled = true
            }
        }.addOnFailureListener{
            Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
            binding.loginBar.visibility = View.GONE
            binding.loginBt.isEnabled = true
        }
    }


}