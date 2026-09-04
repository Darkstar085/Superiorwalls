package com.sipun.superiorwalls

import android.app.Application
import com.sipun.superiorwalls.features.notifications.createWallpaperNotificationChannel

class SuperiorwallsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        createWallpaperNotificationChannel(this)
    }
}
