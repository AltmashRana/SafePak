package com.example.safepak.frontend.gesture

import android.app.ActivityManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.safepak.R
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.frontend.services.CameraService


class EmergencyWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Create a pending Intent for Activity 1
    val start_intent = Intent(context, CameraService::class.java)
    start_intent.putExtra("Front_Request", true)
    val i1 = PendingIntent.getForegroundService(context, 0, start_intent,PendingIntent.FLAG_UPDATE_CURRENT)

    // Create a pending Intent for Activity 2
    val end_intent = Intent(context, CameraService::class.java)
    end_intent.putExtra("Front_Request", true)
    end_intent.action = "stop"
    val i2 = PendingIntent.getForegroundService(context, 0, end_intent,PendingIntent.FLAG_UPDATE_CURRENT)



    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.emergency_widget)
        // Button 1 onClick Function
        .apply{ if(!context.isMyServiceRunning(CameraService::class.java))
                        setOnClickPendingIntent(R.id.widgetlevel2_bt,i1) }
        // Button 2 onClick Function
        .apply {if(context.isMyServiceRunning(CameraService::class.java))
                        setOnClickPendingIntent(R.id.widgetcancel_bt,i2) }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

@Suppress("DEPRECATION")
fun Context.isMyServiceRunning(serviceClass: Class<CameraService>): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == serviceClass.name }
}