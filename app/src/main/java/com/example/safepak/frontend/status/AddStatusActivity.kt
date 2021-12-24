package com.example.safepak.frontend.status

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.databinding.ActivityAddStatusBinding
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.logic.models.Status
import com.example.safepak.logic.session.StorageSession
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AddStatusActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddStatusBinding
    lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStatusBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseFirestore.getInstance()

        var color = ContextCompat.getDrawable(this, R.drawable.chatlist_bg)
        supportActionBar?.setBackgroundDrawable(color);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        FirebaseSession.getCurrentUser { user ->

            Glide.with(this)
                .load(user.img?.let { StorageSession.pathToReference(it) })
                .placeholder(R.drawable.empty_dp)
                .into(binding.addstatusDp)

            binding.statusnameText.text = user.firstname + " " + user.lastname
        }

        binding.addstatusdateText.text = getDate()

        binding.contentBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.addcountText.text = s?.length.toString() + "/500"
            }
        })

        binding.poststatusBt.setOnClickListener{
            var content = binding.contentBox.text.toString()
            var date = binding.addstatusdateText.text.toString()

            binding.addstatusBar.visibility = View.VISIBLE
            binding.poststatusBt.isEnabled = false

            if (content.isNotEmpty() and date.isNotEmpty()){
                createPost(content)
            }
            else {
                Toast.makeText(applicationContext,"Please write something", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NewApi")
    fun getDate() : String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)

        return formatted
    }

    fun createPost(content: String){

        val id = db.collection("statuses").document().id

        val post = Status(id, content,getDate(), FirebaseSession.userID )

        val query = db.collection("statuses").document(id)
        query.set(post).addOnSuccessListener {
            Toast.makeText(applicationContext,"Posted", Toast.LENGTH_SHORT).show()
            binding.addstatusBar.visibility = View.GONE
            finish()
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"Failed", Toast.LENGTH_SHORT).show()
            binding.addstatusBar.visibility = View.GONE
            binding.poststatusBt.isEnabled = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}