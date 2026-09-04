package com.sipun.superiorwalls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sipun.superiorwalls.ui.SuperiorwallsApp
import com.sipun.superiorwalls.ui.theme.SuperiorwallsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperiorwallsTheme {
                SuperiorwallsApp()
            }
        }
    }
}
