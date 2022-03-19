package com.example.safepak.frontend.other

import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safepak.databinding.ActivityFacesBinding
import android.media.MediaMetadataRetriever
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import com.example.safepak.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.android.synthetic.main.progress_dialogue.view.*
import java.nio.ByteBuffer
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class FacesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFacesBinding
    private lateinit var detector : FaceDetector
    private lateinit var all_faces : MutableList<Face>
    private lateinit var faces_map : Array<Bitmap?>
    private lateinit var filename : String
    private lateinit var dialog : AlertDialog
    private lateinit var progress_layout : View
    private var id = 0
    private var done_count = 0

    private lateinit var highAccuracyOpts : FaceDetectorOptions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        filename = intent.extras?.getString("FILE_NAME").toString()

        val builder = AlertDialog.Builder(this);
        progress_layout = layoutInflater.inflate(R.layout.progress_dialogue, null)
        builder.setView(progress_layout);

        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.5f)
            .build()

        detector = FaceDetection.getClient(highAccuracyOpts)
//        detector = FaceDetector(480, 480, 1)
        all_faces = mutableListOf()

        setDialog(true)
        getVideo()

    }

    private fun setDialog(show : Boolean){
        if (show)
            dialog.show()
        else
            dialog.dismiss()
    }

    private fun updateProgress(count : Int) {
        progress_layout.dialogue_percent.text = "${count}%"
    }

    private fun removeRedundant() {
        val result = ArrayList<Bitmap>()

        val temp = faces_map.distinct()


        for(i in 0 until temp.size - 1) {
            if (compareEquivalance(temp[i], temp[i + 1]) < 0.5) {
                result.add(temp[i]!!)
            }
        }

        Thread{
            Runnable {
                for (i in result.indices)
                    saveToInternalStorage(i.toString(), result[i])
                runOnUiThread {
                    setDialog(false)
                    if (result.size > 0)
                        Toast.makeText(this, "Images Saved in Safepak/$filename", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, "No faces found!", Toast.LENGTH_SHORT).show()
                finish()
                }
            }.run()
        }.start()
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

        val mMMR = MediaMetadataRetriever()

        val fps = 15

        val uri = Uri.fromFile(File(getExternalFilesDir(null), "Safepak/${filename}"))
        
        mMMR.setDataSource(this,uri)
        val timeMs = mMMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) // video time in ms

        val totalVideoTime = 1000 * Integer.valueOf(timeMs) // total video time, in uS

        faces_map = arrayOfNulls((totalVideoTime/1000000) * 60 * 30 * 4)

        runAI(totalVideoTime, mMMR, fps, 0)
        runAI(totalVideoTime, mMMR, fps, 90)
        runAI(totalVideoTime, mMMR, fps, 180)
        runAI(totalVideoTime, mMMR, fps, 270)
    }

    private fun runAI(totalVideoTime : Int,mMMR : MediaMetadataRetriever, fps : Int, angle : Int) {
        var time_us = 1
        var flag = false
        var bitmap: Bitmap
        val deltaT = 1000000/fps
        val one_percent = totalVideoTime/100

        Thread {
            Runnable {
                while (time_us < totalVideoTime) {
                    bitmap = mMMR.getFrameAtTime(
                        time_us.toLong(),
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )!!
                    if (!flag) {
                        flag = true
                        var found = false
                        faces_map[id] = bitmap
                        val image = InputImage.fromBitmap(bitmap, angle)
                        detector.process(image)
                            .addOnSuccessListener { faces ->
                                if (faces.size > 0 && !found) {
                                    all_faces.addAll(faces)
                                    runOnUiThread {
//                                        binding.faceImage.setImageBitmap(faces_map[id])
//                                        binding.faceCount.text = all_faces.size.toString()
                                        found = true
                                        id++
                                    }
                                }
                                flag = false
                            }
                            .addOnFailureListener { e ->
                                runOnUiThread {
                                    Toast.makeText(this, "Extraction Failed : $e", Toast.LENGTH_SHORT).show()
                                    flag = false
                                }
                            }
                    }
                    time_us += deltaT
                    runOnUiThread {

                        updateProgress(time_us/one_percent)
                    }
                }
                done_count++
                if (done_count == 4) {
                    Thread.sleep(1000)
                    removeRedundant()
                }
            }.run()
        }.start()
    }

    private fun saveToInternalStorage(name : String, bitmapImage: Bitmap) {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            try {
                val resolver = contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "Safepak" + File.separator + filename.substringBefore('.'))
                val imageuri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                var fos = resolver.openOutputStream(Objects.requireNonNull(imageuri!!))
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                Objects.requireNonNull<OutputStream?>(fos)
            }
            catch(e: Exception) {
                Toast.makeText(this, "Image Not Saved", Toast.LENGTH_SHORT).show()
            }
        } else {
            val directory = File(Environment.DIRECTORY_PICTURES, "Safepak/${filename.substringBefore('.')}")

            if (!directory.exists()) directory.mkdirs()

            val mypath = File(directory, "$name.jpeg")
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(mypath)

                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            } catch (e: Exception) {
                Toast.makeText(this, "Image Not Saved", Toast.LENGTH_SHORT).show()
            } finally {
                try {
                    fos?.close()
                } catch (e: IOException) {
                    Toast.makeText(this, "Image Not Saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}