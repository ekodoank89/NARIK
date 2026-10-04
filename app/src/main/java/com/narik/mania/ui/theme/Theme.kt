package com.narik.mania.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF4CAF50)
private val Red = Color(0xFFE53935)

private val LightScheme = lightColorScheme(primary = Green, error = Red)
private val DarkScheme = darkColorScheme(primary = Green, error = Red)

@Composable
fun NarikTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
