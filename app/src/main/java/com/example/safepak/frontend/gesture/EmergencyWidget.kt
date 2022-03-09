package com.example.safepak.frontend.gesture

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.safepak.R
import com.example.safepak.frontend.home.HomeActivity

/**
 * Implementation of App Widget functionality.
 */
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

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Create a pending Intent for Activity 1
    val i1 : PendingIntent = Intent(context,HomeActivity::class.java).let { intent ->
        PendingIntent.getActivity(context, 0, intent, 0)  }

    // Create a pending Intent for Activity 2
    val i2 : PendingIntent = Intent(context,HomeActivity::class.java).let { intent ->
        PendingIntent.getActivity(context, 0, intent, 0)  }

    // Create a pending Intent for Activity 2
    val i3 : PendingIntent = Intent(context,HomeActivity::class.java).let { intent ->
        PendingIntent.getActivity(context, 0, intent, 0)  }

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.emergency_widget)
        // Button 1 onClick Function
        .apply{setOnClickPendingIntent(R.id.widgetlevel1_bt,i1)}
        // Button 2 onClick Function
        .apply { setOnClickPendingIntent(R.id.widgetlevel2_bt,i2) }
        // Button 2 onClick Function
        .apply { setOnClickPendingIntent(R.id.widgetmedical_bt,i3) }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}