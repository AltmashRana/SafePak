package com.example.safepak.frontend.other

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.safepak.R
import com.example.safepak.databinding.ActivityVideosBinding
import com.example.safepak.frontend.otherAdapters.VideoItem
import com.example.safepak.logic.models.Constants
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.io.File

class VideosActivity : AppCompatActivity() {

    private var videoAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var path : File

    private lateinit var binding : ActivityVideosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideosBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var color = ContextCompat.getDrawable(this, R.drawable.chatlist_bg)
        supportActionBar?.setBackgroundDrawable(color);
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        path = File(getExternalFilesDir(null), "Safepak")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            load_Videos(path)
        } else
            requestCameraPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

    }


    fun load_Videos(directory: File) {

        val fileList  = directory.listFiles()

        binding.videosRecycler.adapter = videoAdapter

        if (fileList.isNotEmpty()) {
            for (i in fileList.indices) {

                if (fileList[i].isDirectory) {
                    load_Videos(fileList[i])

                } else {
                    val name: String = fileList[i].name.lowercase()
                    for (extension in Constants.videoExtensions) {
                        //check the type of file
                        if (name.endsWith(extension)) {
                            videoAdapter.add(VideoItem(fileList[i]))
                            binding.emptyvideosText.visibility = View.GONE
                            //when we found file
                            break
                        }
                    }
                }
            }
        }
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                load_Videos(path)
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}