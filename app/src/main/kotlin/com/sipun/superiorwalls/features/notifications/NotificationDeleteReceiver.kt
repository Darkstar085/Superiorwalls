package com.sipun.superiorwalls.features.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.NotificationManager

class NotificationDeleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uri = intent.getStringExtra(EXTRA_WALLPAPER_URI)?.let(Uri::parse) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        runCatching { context.contentResolver.delete(uri, null, null) }
        if (notificationId >= 0) {
            context.getSystemService(NotificationManager::class.java)?.cancel(notificationId)
        }
    }
}
