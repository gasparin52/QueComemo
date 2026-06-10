package com.quecomemo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Tomato,
    background = Cream,
    surface = Cream,
    onPrimary = Cream,
    onBackground = Ink,
    onSurface = Ink
)

@Composable
fun QueComemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
