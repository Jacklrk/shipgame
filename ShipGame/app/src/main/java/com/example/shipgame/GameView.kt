package com.example.shipgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class GameView(context: Context) : View(context) {

    private val paint = Paint()
    private var shipBitmap: Bitmap
    private val shipSize = 100

    private val shipSprites: Map<Int, Bitmap>
    private val backgroundMap: Bitmap

    private var shipMapX = 0f
    private var shipMapY = 0f

    private var isMoving = false
    private var moveDx = 0f
    private var moveDy = 0f

    // Lista de ángulos disponibles como claves
    private val availableAngles = listOf(
        0, 20, 50, 70, 90, 110, 135, 160,
        180, 210, 230, 250, 270, 290, 310, 340
    )

    init {
        val res = resources

        // Cargar y escalar todos los sprites
        shipSprites = availableAngles.associateWith { angle ->
            val resId = res.getIdentifier("ship_$angle", "drawable", context.packageName)
            val bmp = BitmapFactory.decodeResource(res, resId)
            requireNotNull(bmp) { "Sprite ship_$angle no encontrado" }
            Bitmap.createScaledBitmap(bmp, shipSize, shipSize, true)
        }

        shipBitmap = shipSprites[0]!!

        backgroundMap = BitmapFactory.decodeResource(res, R.drawable.mapa)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        shipMapX = backgroundMap.width / 2f
        shipMapY = backgroundMap.height / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val screenW = width
        val screenH = height
        val centerX = screenW / 2 - shipSize / 2
        val centerY = screenH / 2 - shipSize / 2

        canvas.drawColor(Color.BLACK)

        val offsetX = shipMapX - screenW / 2
        val offsetY = shipMapY - screenH / 2

        val srcLeft = offsetX.toInt().coerceIn(0, backgroundMap.width)
        val srcTop = offsetY.toInt().coerceIn(0, backgroundMap.height)
        val srcRight = (offsetX + screenW).toInt().coerceIn(0, backgroundMap.width)
        val srcBottom = (offsetY + screenH).toInt().coerceIn(0, backgroundMap.height)

        val srcRect = Rect(srcLeft, srcTop, srcRight, srcBottom)

        val destLeft = if (offsetX < 0) -offsetX.toInt() else 0
        val destTop = if (offsetY < 0) -offsetY.toInt() else 0
        val destRight = destLeft + srcRect.width()
        val destBottom = destTop + srcRect.height()

        val destRect = Rect(destLeft, destTop, destRight, destBottom)

        if (srcRect.width() > 0 && srcRect.height() > 0) {
            canvas.drawBitmap(backgroundMap, srcRect, destRect, null)
        }

        canvas.drawBitmap(shipBitmap, centerX.toFloat(), centerY.toFloat(), paint)

        if (isMoving) {
            shipMapX += moveDx
            shipMapY += moveDy
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dx = event.x - width / 2
                val dy = event.y - height / 2

                val angle = (Math.toDegrees(atan2(dx, -dy).toDouble()) + 360).toInt() % 360

                val directionAngle = availableAngles.minByOrNull { abs(it - angle) } ?: 0
                shipBitmap = shipSprites[directionAngle] ?: shipBitmap

                val speed = 10f
                val norm = sqrt(dx * dx + dy * dy)
                moveDx = speed * (dx / norm)
                moveDy = speed * (dy / norm)

                isMoving = true
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                isMoving = false
            }
        }
        return true
    }

}
