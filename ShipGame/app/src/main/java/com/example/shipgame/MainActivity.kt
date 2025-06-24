package com.example.shipgame

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import android.util.Log
import com.google.android.exoplayer2.PlaybackException

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1️⃣ super
        super.onCreate(savedInstanceState)

        /* ─── IMERSIÓN Y PANTALLA COMPLETA ─── */
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Quita status-bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // Quita action-bar de AppCompat
        supportActionBar?.hide()

        // Horizontal
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Barra de navegación oculta + sticky
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        /* ─── CONTENIDO ─── */
        setContentView(R.layout.activity_main)

        // Botón Inicio
        findViewById<Button>(R.id.start_button).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        // Salir
        findViewById<Button>(R.id.btn_exit).setOnClickListener { finish() }

        // Foto
        val avatar = findViewById<ImageView>(R.id.saturn_top)

        // Vídeo FLV
        playerView = findViewById(R.id.player_view)
        player = ExoPlayer.Builder(this).build().also { playerView.player = it }
        player.setMediaItem(MediaItem.fromUri("asset:///video/demo.mp4"))
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.prepare()
        player.playWhenReady = true
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("EXO", "Error: ${error.errorCodeName} - ${error.cause}")
            }
        })

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

    override fun onStop() {
        super.onStop(); player.playWhenReady = false; player.pause()
    }
    override fun onDestroy() {
        super.onDestroy(); player.release()
    }
}
