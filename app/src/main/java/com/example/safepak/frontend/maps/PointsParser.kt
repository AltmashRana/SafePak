package com.example.safepak.frontend.maps

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap
import kotlin.coroutines.CoroutineContext


class PointsParser(mContext: Context, directionMode: String) : CoroutineScope{
    var taskCallback: TaskLoadedCallback = mContext as TaskLoadedCallback
    var directionMode = "driving"

    init {
        this.directionMode = directionMode
    }

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job // to run code in Main(UI) Thread

    // call this method to cancel a coroutine when you don't need it anymore,
    // e.g. when user closes the screen
    fun cancel() {
        job.cancel()
    }

    fun execute(vararg strings: String) = launch {
        onPreExecute()
        val result = doInBackground(*strings) // runs in background thread without blocking the Main Thread
        onPostExecute(result)
    }

    private suspend fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? =
        withContext(Dispatchers.IO) { // to run code in Background Thread
            // do async work
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                Log.d("mylog", jsonData[0])
                val parser = DataParser()
                Log.d("mylog", parser.toString())

                // Starts parsing data
                routes = parser.parse(jObject)
                Log.d("mylog", "Executing routes")
                Log.d("mylog", routes.toString())
            } catch (e: Exception) {
                Log.d("mylog", e.toString())
                e.printStackTrace()
            }
            return@withContext routes
        }

    // Runs on the Main(UI) Thread
    private fun onPreExecute() {
        // show progress
    }

    // Runs on the Main(UI) Thread
    private fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
        // hide progress
        var points: ArrayList<LatLng?>
        var lineOptions: PolylineOptions? = null
        // Traversing through all the routes
        for (i in result!!.indices) {
            points = ArrayList()
            lineOptions = PolylineOptions()
            // Fetching i-th route
            val path = result[i]
            // Fetching all the points in i-th route
            for (j in path.indices) {
                val point = path[j]
                val lat = point["lat"]!!.toDouble()
                val lng = point["lng"]!!.toDouble()
                val position = LatLng(lat, lng)
                points.add(position)
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points)
            if (directionMode.equals("walking", ignoreCase = true)) {
                lineOptions.width(10f)
                lineOptions.color(Color.BLUE)
            } else {
                lineOptions.width(25f)
                lineOptions.color(Color.BLUE)
            }
            Log.d("mylog", "onPostExecute lineoptions decoded")
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            //mMap.addPolyline(lineOptions);
            taskCallback.onTaskDone(lineOptions)
        } else {
            Log.d("mylog", "without Polylines drawn")
        }
    }
}
