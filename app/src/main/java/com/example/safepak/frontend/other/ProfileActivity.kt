package com.example.safepak.frontend.other

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import com.example.safepak.databinding.ActivityProfileBinding

import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.logic.session.StorageSession
import java.io.ByteArrayOutputStream



class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var color = ContextCompat.getDrawable(this,R.drawable.chatlist_bg)
        supportActionBar?.setBackgroundDrawable(color);
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        binding.dpeditBt.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
        }

        binding.profilefinalizeBt.setOnClickListener{
            binding.profileBar.visibility = View.VISIBLE
            binding.profilefinalizeBt.isEnabled = false

            var name = binding.profilenameBox.editText?.text.toString().split(" ")
            var email = binding.profileemailBox.editText?.text.toString()
            var password = binding.profilepasswordBox.editText?.text.toString()
            var gender = binding.profilegenderSpinner.selectedItem.toString()
            var blood = binding.profilebloodSpinner.selectedItem.toString()
            var willing = binding.profilewillingTick.isChecked


            if (::selectedImageBytes.isInitialized)
                StorageSession.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                    FirebaseSession.updateCurrentUser(name[0], name[1],gender, blood, password
                        ,"", email, willing, imagePath)
                }
            else
                FirebaseSession.updateCurrentUser(name[0], name[1],gender, blood, password
                    ,"",email, willing, null)

            binding.profileBar.visibility = View.GONE
            binding.profilefinalizeBt.isEnabled = true
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            Glide.with(this)
                .load(selectedImageBytes)
                .into(binding.profileDp)


            pictureJustChanged = true
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseSession.getCurrentUser { user ->
            binding.profilenameBox.editText?.setText("${user.firstname} ${user.lastname}")
            binding.profileemailBox.editText?.setText(user.email)
            binding.profilepasswordBox.editText?.setText(user.password)
            when (user.gender) {
                "Male" -> {
                    binding.profilegenderSpinner.setSelection(0)
                }
                "Female" -> {
                    binding.profilegenderSpinner.setSelection(1)
                }
                "Other" -> {
                    binding.profilegenderSpinner.setSelection(2)
                }
            }
            when (user.bloodgroup) {
                "A+" -> {
                    binding.profilebloodSpinner.setSelection(0)
                }
                "B+" -> {
                    binding.profilebloodSpinner.setSelection(1)
                }
                "AB+" -> {
                    binding.profilebloodSpinner.setSelection(2)
                }
                "O+" -> {
                    binding.profilebloodSpinner.setSelection(3)
                }
                "A-" -> {
                    binding.profilebloodSpinner.setSelection(4)
                }
                "B-" -> {
                    binding.profilebloodSpinner.setSelection(5)
                }
                "AB-" -> {
                    binding.profilebloodSpinner.setSelection(6)
                }
                "O-" -> {
                    binding.profilebloodSpinner.setSelection(7)
                }
            }
            binding.profilewillingTick.isChecked = user.willingToDonate == true

            if (!pictureJustChanged && user.img != null) {

                Glide.with(this)
                    .load(StorageSession.pathToReference(user.img!!))
                    .placeholder(R.drawable.empty_dp)
                    .into(binding.profileDp)
            }
            binding.profileBar.visibility = View.GONE
            binding.profilefinalizeBt.isEnabled = true

            binding.profileDp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
            binding.profilefinalizeBt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))


        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}