package com.example.shipgame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import org.json.JSONObject

object AtlasManager {

    private lateinit var atlas: Bitmap
    private val frames = mutableMapOf<String, List<Rect>>()   // ej. "explosion"

    /** Cargar UNA vez (p. ej. en GameActivity.onCreate) */
    fun load(context: Context) {
        // --- 1) textura ---
        context.assets.open("atlas/res_explosion.png").use {
            atlas = BitmapFactory.decodeStream(it)
        }

        // --- 2) metadatos ---
        val jsonStr = context.assets.open("atlas/res_explosion.json")
            .bufferedReader().readText()
        val json = JSONObject(jsonStr)
        val frameArr = json.getJSONArray("frames")

        // JSON Array standard de TexturePacker
        val rects = mutableListOf<Rect>()
        for (i in 0 until frameArr.length()) {
            val fr = frameArr.getJSONObject(i)
            val r  = fr.getJSONObject("frame")
            rects += Rect(
                r.getInt("x"),
                r.getInt("y"),
                r.getInt("x") + r.getInt("w"),
                r.getInt("y") + r.getInt("h")
            )
        }
        frames["explosion"] = rects
    }

    fun bitmap(): Bitmap = atlas
    fun frames(key: String): List<Rect> = frames[key] ?: emptyList()
}
