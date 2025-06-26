package com.example.shipgame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import org.json.JSONObject

object AtlasManager {

    /* -- varios atlas -- */
    private val atlases = mutableMapOf<String, Bitmap>()      // key → bitmap
    private val frames  = mutableMapOf<String, List<Rect>>()  // key → rect list

    /**
     * Llámalo una sola vez (ej. en `GameActivity.onCreate`)
     */
    fun load(context: Context) {
        loadAtlas(context, "res_explosion")
        loadAtlas(context, "escudoreg")
        loadAtlas(context, "vidareg")
    }

    /* ---------- helpers ---------- */

    private fun loadAtlas(ctx: Context, key: String) {
        // 1) bitmap
        val bmp = ctx.assets.open("atlas/$key.png").use {
            BitmapFactory.decodeStream(it)
        }
        atlases[key] = bmp

        // 2) JSON → lista de Rect
        val jsonStr = ctx.assets.open("atlas/$key.json")
            .bufferedReader().readText()
        val jsonArr = JSONObject(jsonStr).getJSONArray("frames")

        val rectList = mutableListOf<Rect>()
        for (i in 0 until jsonArr.length()) {
            val fr  = jsonArr.getJSONObject(i).getJSONObject("frame")
            rectList += Rect(
                fr.getInt("x"),
                fr.getInt("y"),
                fr.getInt("x") + fr.getInt("w"),
                fr.getInt("y") + fr.getInt("h")
            )
        }
        frames[key] = rectList
    }

    /* ---------- getters ---------- */
    fun bitmap(key: String): Bitmap?          = atlases[key]
    fun frames(key: String): List<Rect>       = frames[key] ?: emptyList()
}
