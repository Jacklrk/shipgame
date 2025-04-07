package com.example.shipgame.ui.theme

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.shipgame.R
import android.content.Context

// Define tus estilos de texto como en el sistema tradicional
data class TextStyle(
    val fontFamily: Typeface? = null,
    val fontWeight: Int = Typeface.NORMAL,
    val fontSize: Float = 16f,
    val lineHeight: Float = 24f,
    val letterSpacing: Float = 0.5f
)

// Define la tipografía de la aplicación
fun getTypography(context: Context): TypographyStyles {
    return TypographyStyles(
        bodyLarge = TextStyle(
            fontFamily = ResourcesCompat.getFont(context, R.font.roboto_regular), // Utilizando una fuente personalizada desde `res/font`
            fontWeight = Typeface.NORMAL,
            fontSize = 16f,
            lineHeight = 24f,
            letterSpacing = 0.5f
        )
    )
}

// Clase que representa los estilos tipográficos de la aplicación
data class TypographyStyles(
    val bodyLarge: TextStyle
    // Agregar más estilos de texto según sea necesario
)

// Utilizar los estilos en una actividad o vista
fun applyTypographyToTextView(textView: TextView, context: Context) {
    val typography = getTypography(context)
    textView.setTypeface(typography.bodyLarge.fontFamily, typography.bodyLarge.fontWeight)
    textView.textSize = typography.bodyLarge.fontSize
    // Agregar más configuraciones si es necesario
}
