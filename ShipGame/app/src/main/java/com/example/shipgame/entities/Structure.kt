package com.example.shipgame.entities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Structure(
    val bitmap: Bitmap,
    private val x: Float,
    private val y: Float,
    private val size: Int          // lado en píxeles en pantalla
) {
    private val safeRadius = size * 0.7f
    fun getX() = x
    fun getY() = y
    fun radius() = safeRadius
    fun contains(px: Float, py: Float): Boolean {
        val dx = px - x;  val dy = py - y
        return dx*dx + dy*dy <= safeRadius*safeRadius
    }
    fun draw(canvas: Canvas, paint: Paint, cameraX: Float, cameraY: Float,
             screenCenterX: Int, screenCenterY: Int) {

        val scrX = (x - cameraX) + screenCenterX
        val scrY = (y - cameraY) + screenCenterY

        val dst = RectF(
            scrX - size/2, scrY - size/2,
            scrX + size/2, scrY + size/2
        )
        canvas.drawBitmap(bitmap, null, dst, paint)
    }
}
