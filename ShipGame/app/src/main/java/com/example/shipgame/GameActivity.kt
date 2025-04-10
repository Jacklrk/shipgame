package com.example.shipgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.Window
import android.view.WindowManager
class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Llamar primero a super.onCreate
        super.onCreate(savedInstanceState)

        // Ocultar la barra de título
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Pantalla completa (sin barra de estado)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Quitar ActionBar si existe
        supportActionBar?.hide()

        // Forzar orientación horizontal
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Activar modo inmersivo (barra de navegación oculta)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        // Establecer vista del juego
        setContentView(GameView(this))  // Ya está aquí, no es necesario llamarlo dos veces.
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }
}
