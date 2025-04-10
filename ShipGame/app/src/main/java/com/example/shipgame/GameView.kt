package com.example.shipgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ScaleGestureDetector
import kotlin.math.*

class GameView(context: Context) : View(context) {

    private val paint = Paint()
    private var shipBitmap: Bitmap
    private val shipSize = 100

    private val shipSprites: Map<Int, Bitmap>  // Sprites por ángulo
    private val backgroundMap: Bitmap

    private var shipMapX = 0f
    private var shipMapY = 0f

    private var isMoving = false
    private var moveDx = 0f
    private var moveDy = 0f

    // Zoom variables
    private var scaleFactor = 1f
    private val scaleGestureDetector: ScaleGestureDetector

    // HUD: Minimapa y su visibilidad
    private var showMinimap = false
    private val minimapSize = 400 // Tamaño del minimapa en píxeles

    // Rectángulo para el botón del HUD
    private val hudButtonRect = RectF(50f, 50f, 150f, 150f)

    // Imagen del icono del minimapa
    private val minimapButtonIcon: Bitmap

    init {
        val res = resources

        // Carga la imagen del botón
        minimapButtonIcon = BitmapFactory.decodeResource(res, R.drawable.minimap)

        // Carga de los 16 sprites por ángulo (cada 20 grados)
        shipSprites = listOf(
            0, 20, 50, 70, 90, 110, 135, 160,
            180, 210, 230, 250, 270, 290, 310, 340
        ).associateWith { angle ->
            val resId = res.getIdentifier("ship_${angle}", "drawable", context.packageName)
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, resId), shipSize, shipSize, true)
        }

        // Valor inicial
        shipBitmap = shipSprites[0]!!

        // Mapa de fondo
        backgroundMap = BitmapFactory.decodeResource(res, R.drawable.mapa)

        // Crear el detector de gestos de escala
        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = max(0.1f, min(scaleFactor, 5.0f)) // Limitar el rango de zoom entre 0.1x y 5x
                invalidate() // Redibujar la vista
                return true
            }
        })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
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

        // Escalar el fondo para hacer zoom
        val scaledBackground = Bitmap.createScaledBitmap(backgroundMap, (backgroundMap.width * scaleFactor).toInt(), (backgroundMap.height * scaleFactor).toInt(), true)
        if (srcRect.width() > 0 && srcRect.height() > 0) {
            canvas.drawBitmap(scaledBackground, srcRect, destRect, null)
        }

        // Escalar la nave para hacer zoom
        val scaledShipBitmap = Bitmap.createScaledBitmap(shipBitmap, (shipSize * scaleFactor).toInt(), (shipSize * scaleFactor).toInt(), true)
        canvas.drawBitmap(scaledShipBitmap, centerX.toFloat(), centerY.toFloat(), paint)

        // Mostrar minimapa si se ha activado
        if (showMinimap) {
            drawMinimap(canvas)
        }

        if (isMoving) {
            shipMapX += moveDx
            shipMapY += moveDy
            invalidate()
        }

        // Dibuja el icono del minimapa sobre el botón
        canvas.drawBitmap(minimapButtonIcon, null, hudButtonRect, paint)
    }

    private fun drawMinimap(canvas: Canvas) {
        // Dibujar minimapa en la esquina superior derecha
        val minimapX = (width - minimapSize - 50).toFloat() // Asegurar que sea Float
        val minimapY = 50f

        // Escala para ajustar el tamaño del minimapa
        val scaleX = minimapSize / backgroundMap.width.toFloat()
        val scaleY = minimapSize / backgroundMap.height.toFloat()

        // Dibuja el mapa reducido
        val minimapRect = RectF(minimapX, minimapY, minimapX + minimapSize, minimapY + minimapSize)
        canvas.drawBitmap(backgroundMap, null, minimapRect, null)

        paint.color = Color.RED
        // Dibuja la nave en el minimapa
        val minimapShipX = minimapX + shipMapX * scaleX
        val minimapShipY = minimapY + shipMapY * scaleY
        val minimapShipSize = 5f // Tamaño de la nave en el minimapa
        canvas.drawCircle(minimapShipX, minimapShipY, minimapShipSize, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event) // Maneja el gesto de zoom

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Revisar si se toca el botón del HUD
                if (hudButtonRect.contains(event.x, event.y)) {
                    showMinimap = !showMinimap
                    invalidate()
                } else {
                    val dx = event.x - width / 2
                    val dy = event.y - height / 2

                    val angle = (Math.toDegrees(atan2(dx, -dy).toDouble()) + 360).toInt() % 360

                    val directionAngle = listOf(
                        0, 20, 50, 70, 90, 110, 135, 160,
                        180, 210, 230, 250, 270, 290, 310, 340
                    ).minByOrNull { abs(it - angle) } ?: 0

                    shipBitmap = shipSprites[directionAngle] ?: shipBitmap

                    val speed = 10f
                    val norm = sqrt(dx * dx + dy * dy)
                    moveDx = speed * (dx / norm)
                    moveDy = speed * (dy / norm)

                    isMoving = true
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                isMoving = false
            }
        }
        return true
    }
}
