package com.example.shipgame

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Bitmap

class FrameAnimation(
    private val rects: List<Rect>,
    private val fps: Int = 15
) {
    private var time = 0f
    val finished get() = (time * fps).toInt() >= rects.size

    fun reset() { time = 0f }
    fun update(dt: Float) { time += dt }

    fun draw(c: Canvas, atlas: Bitmap, x: Float, y: Float) {
        if (finished || rects.isEmpty()) return
        val idx  = ((time * fps).toInt()).coerceAtMost(rects.lastIndex)
        val src  = rects[idx]
        val dest = RectF(x, y, x + src.width(), y + src.height())
        c.drawBitmap(atlas, src, dest, null)
    }
}
