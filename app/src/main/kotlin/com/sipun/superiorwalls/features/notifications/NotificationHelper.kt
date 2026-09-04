package com.sipun.superiorwalls.features.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.sipun.superiorwalls.R

const val WALLPAPER_NOTIFICATION_CHANNEL_ID = "wallpaper_downloads"
private const val WALLPAPER_NOTIFICATION_BASE_ID = 4200
const val EXTRA_WALLPAPER_URI = "wallpaper_uri"
const val EXTRA_NOTIFICATION_ID = "notification_id"

fun createWallpaperNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        WALLPAPER_NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = context.getString(R.string.notification_channel_description)
    }
    context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
}

fun notifyWallpaperSaved(context: Context, uri: Uri, displayName: String, bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    createWallpaperNotificationChannel(context)
    val notificationId = WALLPAPER_NOTIFICATION_BASE_ID + (uri.toString().hashCode() and 0x7fffffff) % 100000
    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val openPendingIntent = PendingIntent.getActivity(
        context,
        notificationId,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val deleteIntent = Intent(context, NotificationDeleteReceiver::class.java).apply {
        putExtra(EXTRA_WALLPAPER_URI, uri.toString())
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }
    val deletePendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId,
        deleteIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val notification = Notification.Builder(context, WALLPAPER_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle(displayName)
        .setContentText(context.getString(R.string.notification_wallpaper_saved))
        .setCategory(Notification.CATEGORY_STATUS)
        .setAutoCancel(true)
        .setOnlyAlertOnce(true)
        .setContentIntent(openPendingIntent)
        .setLargeIcon(bitmap)
        .setStyle(
            Notification.BigPictureStyle()
                .bigPicture(bitmap)
                .setBigContentTitle(displayName)
                .setSummaryText(context.getString(R.string.notification_wallpaper_saved)),
        )
        .addAction(
            Notification.Action.Builder(
                android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_menu_delete),
                context.getString(R.string.notification_delete),
                deletePendingIntent,
            ).build(),
        )
        .build()
    context.getSystemService(NotificationManager::class.java)?.notify(notificationId, notification)
}

fun cancelWallpaperNotifications(context: Context) {
    context.getSystemService(NotificationManager::class.java)?.cancelAll()
}
