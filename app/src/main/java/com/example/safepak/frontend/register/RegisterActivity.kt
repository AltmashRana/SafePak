package com.example.safepak.frontend.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.safepak.databinding.ActivityRegisterBinding
import com.example.safepak.frontend.login.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseFirestore.getInstance()

        binding.registeredBt.setOnClickListener {

            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.submitBt.setOnClickListener {

            val name = binding.fullnameText.editText?.text.toString()
            val phone = binding.phoneText.editText?.text.toString()
            val password = binding.passwordText.editText?.text.toString()
            val cpassword = binding.cpasswordText.editText?.text.toString()
            val cnic = binding.cnicText.editText?.text.toString()

            val namePattern = "[a-zA-Z0-9.-]+ [a-zA-Z0-9.-]+"
            if (name.let { it.isEmpty() or (!it.matches(namePattern.toRegex()))}) {
                binding.fullnameText.error = "Enter a full name"
                return@setOnClickListener
            } else {
                binding.fullnameText.error = null
                binding.fullnameText.isErrorEnabled = false
            }
            if (phone.length != 11) {
                binding.phoneText.error = "Enter a valid phone number"
                return@setOnClickListener
            } else {
                binding.phoneText.error = null
                binding.phoneText.isErrorEnabled = false
            }
            if (password.let { it.isEmpty() or (it.length < 8) }) {
                binding.passwordText.error = "Enter a 8-digit password"
                return@setOnClickListener
            } else {
                binding.passwordText.error = null
                binding.passwordText.isErrorEnabled = false
            }
            if (cpassword.let { !it.equals(password) }) {
                binding.cpasswordText.error = "password does not match"
                return@setOnClickListener
            } else {
                binding.cpasswordText.error = null
                binding.cpasswordText.isErrorEnabled = false
            }
            if (cnic.length != 13) {
                binding.cnicText.error = "Enter a valid cnic"
                return@setOnClickListener
            } else {
                binding.cnicText.error = null
                binding.cnicText.isErrorEnabled = false
            }

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
                    Toast.makeText(this@RegisterActivity, "User Already Exist", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}