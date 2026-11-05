package com.clubcat.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clubcat.player.ui.ClubCatApp
import com.clubcat.player.ui.theme.ClubCatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ClubCatTheme {
                ClubCatApp()
            }
        }
    }
}
