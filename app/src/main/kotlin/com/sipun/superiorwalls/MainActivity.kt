package com.sipun.superiorwalls

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sipun.superiorwalls.ui.SuperiorwallsApp

class MainActivity : ComponentActivity() {
    private var incomingImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        incomingImage = intent.imageUri()
        setContent {
            SuperiorwallsApp(importedImage = incomingImage)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingImage = intent.imageUri()
        recreate()
    }

    private fun Intent.imageUri(): Uri? = when (action) {
        Intent.ACTION_VIEW -> data
        Intent.ACTION_SEND -> getParcelableExtra(Intent.EXTRA_STREAM)
        else -> null
    }?.takeIf { it.scheme == "content" || it.scheme == "file" }
}
