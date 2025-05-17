package com.example.shipgame.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.core.content.ContextCompat

// Colores definidos sin Compose
val Purple80 = Color.parseColor("#D0BCFF")
val PurpleGrey80 = Color.parseColor("#CCC2DC")
val Pink80 = Color.parseColor("#EFB8C8")

val Purple40 = Color.parseColor("#6650a4")
val PurpleGrey40 = Color.parseColor("#625b71")
val Pink40 = Color.parseColor("#7D5260")

// Función para cambiar el tema de la aplicación
fun applyAppTheme(activity: Activity, isDarkTheme: Boolean) {
    // Aplicar el esquema de colores según el tema (oscuro o claro)
    val colorScheme = if (isDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Aquí puedes aplicar los colores a las vistas de tu aplicación usando `colorScheme`
    // Ejemplo: activity.window.statusBarColor = colorScheme.primary
    activity.window.statusBarColor = colorScheme.primary
}

// Definición de los esquemas de color
data class ColorScheme(val primary: Int, val secondary: Int, val tertiary: Int)

val DarkColorScheme = ColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val LightColorScheme = ColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Función para determinar si el sistema está en modo oscuro
fun isSystemInDarkTheme(context: Context): Boolean {
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}
