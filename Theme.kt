package com.clubcat.player.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Paleta propia de ClubCat, usada cuando no hay colores dinámicos (Android < 12).
private val ClubCatLight = lightColorScheme(
    primary = Color(0xFF8E3CF0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDCFF),
    onPrimaryContainer = Color(0xFF2A0055),
    secondary = Color(0xFFE5368C),
    onSecondary = Color.White,
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
)

private val ClubCatDark = darkColorScheme(
    primary = Color(0xFFD3BBFF),
    onPrimary = Color(0xFF45009A),
    primaryContainer = Color(0xFF6420C5),
    onPrimaryContainer = Color(0xFFEBDCFF),
    secondary = Color(0xFFFFB0CF),
    onSecondary = Color(0xFF650035),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
)

@Composable
fun ClubCatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ClubCatDark
        else -> ClubCatLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
