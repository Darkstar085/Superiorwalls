@file:Suppress("DEPRECATION")

package dev.jahir.frames.extensions.context

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.annotation.IdRes

inline fun Activity.restart(intentBuilder: Intent.() -> Unit = {}) {
    val i = Intent(this, this::class.java)
    intent?.extras?.let { i.putExtras(it) }
    i.intentBuilder()
    i.flags = i.flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(i)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

inline var Activity.statusBarColor: Int
    get() = window.statusBarColor
    set(value) {
        window.statusBarColor = value
    }

inline var Activity.statusBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() {
        return window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR > 0
    }
    @SuppressLint("InlinedApi")
    set(value) {
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility =
            if (value) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

inline var Activity.navigationBarColor: Int
    get() = window.navigationBarColor
    set(value) {
        window.navigationBarColor = value
    }

inline var Window.navigationBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() {
        return decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR > 0
    }
    @SuppressLint("InlinedApi")
    set(value) {
        val flags = decorView.systemUiVisibility
        decorView.systemUiVisibility =
            if (value) flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
    }

inline var Activity.navigationBarLight: Boolean
    @SuppressLint("InlinedApi")
    get() = window.navigationBarLight
    @SuppressLint("InlinedApi")
    set(value) {
        window.navigationBarLight = value
    }

inline fun <reified T : View> Activity.findView(
    @IdRes id: Int,
    logException: Boolean = false
): Lazy<T?> {
    return lazy {
        try {
            findViewById<T>(id)
        } catch (e: Exception) {
            if (logException) e.printStackTrace()
            null
        }
    }
}
