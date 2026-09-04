package com.sipun.superiorwalls

import android.app.Application

class SuperiorwallsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}
