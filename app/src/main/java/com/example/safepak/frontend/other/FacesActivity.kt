package com.example.safepak.frontend.other

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safepak.databinding.ActivityFacesBinding
import android.media.MediaMetadataRetriever
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import java.nio.ByteBuffer


class FacesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFacesBinding
    private lateinit var detector : FaceDetector
    private lateinit var all_faces : MutableList<Face>
    private lateinit var faces_map : ArrayList<Bitmap>
    private lateinit var filename : String

    private lateinit var highAccuracyOpts : FaceDetectorOptions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        filename = intent.extras?.getString("FILE_NAME").toString()


        highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.5f)
            .build()

        detector = FaceDetection.getClient(highAccuracyOpts)
//        detector = FaceDetector(480, 480, 1)
        all_faces = mutableListOf()
        faces_map = ArrayList(10000)
        getVideo()

    }

    private fun removeRedundant() {
//        val result = ArrayList<Bitmap>()
//
//        for(i in faces_map) {
//            for (j in faces_map) {
//                if (i.key != j.key) {
//                    if (compareEquivalance(i.value, j.value) >= 0.5) {
//                        faces_map.remove(i.key)
//                    }
//                }
//            }
//        }
//        Thread{
//            Runnable {
//                var count = 0
//                for (face in faces_map.values){
//                    runOnUiThread {
//                        binding.faceImage.setImageBitmap(face)
//                        binding.faceCount.text = count.toString()
//                        count++
//                    }
//                    Thread.sleep(2000)
//                }
//            }.run()
//        }.start()
    }

    fun compareEquivalance(bitmap1: Bitmap?, bitmap2: Bitmap?): Float {
        if (bitmap1 == null || bitmap2 == null || bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height) {
            return 0f
        }
        val buffer1: ByteBuffer = ByteBuffer.allocate(bitmap1.height * bitmap1.rowBytes)
        bitmap1.copyPixelsToBuffer(buffer1)
        val buffer2 = ByteBuffer.allocate(bitmap2.height * bitmap2.rowBytes)
        bitmap2.copyPixelsToBuffer(buffer2)
        val array1: ByteArray = buffer1.array()
        val array2: ByteArray = buffer2.array()
        val len = array1.size // array1 and array2 will be of some length.
        var count = 0
        for (i in 0 until len) {
            if (array1[i] == array2[i]) {
                count++
            }
        }
        return count.toFloat() / len
    }

    fun getVideo(){
        var bitmap: Bitmap
        val mMMR = MediaMetadataRetriever()

        val fps = 30

        val deltaT = 1000000/fps
        val uri = Uri.fromFile(File(getExternalFilesDir(null), "Safepak/${filename}"))
        
        mMMR.setDataSource(this,uri)
        val timeMs = mMMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) // video time in ms

        val totalVideoTime = 1000 * Integer.valueOf(timeMs) // total video time, in uS

        var time_us = 1
        var flag = false

        var id = 0
        Thread {
            Runnable {
                while (time_us < totalVideoTime) {
                    bitmap = mMMR.getFrameAtTime(
                        time_us.toLong(),
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )!!
                        if (!flag) {
                            flag = true
                            var angle = 0
                            var found = false
                            while (angle <= 180 && !found) {
                                    faces_map[id] = bitmap
                                    val image = InputImage.fromBitmap(bitmap, angle)
                                    detector.process(image)
                                        .addOnSuccessListener { faces ->
                                            if (faces.size > 0 && !found) {
                                                all_faces.addAll(faces)
                                                runOnUiThread {
                                                    binding.faceImage.setImageBitmap(faces_map[id.toString()])
                                                    binding.faceCount.text = all_faces.size.toString()
                                                    found = true
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            runOnUiThread {
                                                Toast.makeText(
                                                    this,
                                                    "Extraction Failed : $e",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                flag = false
                                            }
                                        }
                                angle += 90
                            }
                                flag = false
                    }
//                    Thread.sleep(30)
                    time_us += deltaT
                    runOnUiThread {
                        binding.faceTime.text = time_us.toString()
                    }
                }
                removeRedundant()
            }.run()
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}