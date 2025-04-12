package com.example.shipgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import com.example.shipgame.entities.Ship
import com.example.shipgame.entities.Enemy


class GameView(context: Context) : View(context) {

    private val ship = Ship()
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

    //*************** Enemigo
    private val enemies = mutableListOf<Enemy>()
    private val enemyBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.lordakium)
    private val enemySize = 80



    // HUD: Minimapa y su visibilidad
    private var showMinimap = false
    private val minimapSize = 400 // Tamaño del minimapa en píxeles

    // Rectángulo para el botón del HUD
    private val hudButtonRect = RectF(50f, 50f, 150f, 150f)

    // Imagen del icono del minimapa
    private val minimapButtonIcon: Bitmap

    private val hudLifeButtonRect = RectF(50f, 160f, 150f, 260f)  // Debajo del minimapa
    private lateinit var lifeButtonIcon: Bitmap


    // HUD de vida/escudo
    private var showHudStatus = false
    private var health = 80
    private var shield = 60


    init {
        val res = resources

        // Carga la imagen del botónminimapa icon
        minimapButtonIcon = BitmapFactory.decodeResource(res, R.drawable.minimap)
        //**********************************
        //Carga imagen del boton de vidaship icon
        lifeButtonIcon = BitmapFactory.decodeResource(res, R.drawable.lifeship)
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

    }
    init {
        val rng = java.util.Random()
        repeat(5) {
            val x = rng.nextInt(backgroundMap.width).toFloat()
            val y = rng.nextInt(backgroundMap.height).toFloat()
            enemies.add(Enemy(x, y, 100, 50, 0, 100, 100))
        }
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
        // Movimiento continuo del jugador (si se está moviendo)
        if (isMoving) {
            shipMapX += moveDx
            shipMapY += moveDy
        }
        //******************** Enemigo
        //*********** Dibujar enemigos
        for (enemy in enemies) {
            val enemyScreenX = (enemy.getX() - shipMapX) + centerX
            val enemyScreenY = (enemy.getY() - shipMapY) + centerY

            val enemyRect = RectF(
                enemyScreenX - enemySize / 2,
                enemyScreenY - enemySize / 2,
                enemyScreenX + enemySize / 2,
                enemyScreenY + enemySize / 2
            )
            canvas.drawBitmap(enemyBitmap, null, enemyRect, paint)

            // Movimiento del enemigo hacia el jugador
            val enemyDx = shipMapX - enemy.getX()
            val enemyDy = shipMapY - enemy.getY()
            val distanceenemy = sqrt(enemyDx * enemyDx + enemyDy * enemyDy)

            val detectionRange = 1500f       // Rango de detección para seguir
            val stopRange = 150f             // Rango mínimo para detenerse (colisión simulada)
            val enemySpeed = 5f             // Velocidad del enemigo

            if (distanceenemy < detectionRange && distanceenemy > stopRange) {
                val moveX = enemySpeed * (enemyDx / distanceenemy)
                val moveY = enemySpeed * (enemyDy / distanceenemy)
                enemy.moveBy(moveX, moveY)
            }
            invalidate()
        }

        /*
        val enemyScreenX = (enemy.getX() - shipMapX) + centerX
        val enemyScreenY = (enemy.getY() - shipMapY) + centerY
        val enemyRect = RectF(
            enemyScreenX - enemySize / 2,
            enemyScreenY - enemySize / 2,
            enemyScreenX + enemySize / 2,
            enemyScreenY + enemySize / 2
        )
        canvas.drawBitmap(enemyBitmap, null, enemyRect, paint)
        // Movimiento continuo del jugador (si se está moviendo)
        if (isMoving) {
            shipMapX += moveDx
            shipMapY += moveDy
        }

        // Movimiento del enemigo (independiente de isMoving)
        val enemyDx = shipMapX - enemy.getX()
        val enemyDy = shipMapY - enemy.getY()
        val distanceenemy = sqrt((enemyDx * enemyDx + enemyDy * enemyDy))

        val detectionRange = 1500f       // Rango de detección para seguir
        val stopRange = 150f             // Rango mínimo para detenerse (colisión simulada)
        val enemySpeed = 5f             // Velocidad del enemigo

        if (distanceenemy < detectionRange && distanceenemy > stopRange) {
            val moveX = enemySpeed * (enemyDx / distanceenemy)
            val moveY = enemySpeed * (enemyDy / distanceenemy)
            enemy.moveBy(moveX, moveY)
        }


        invalidate()  */
        // Redibuja siempre el canvas para permitir movimiento continuo



        // Mostrar minimapa si se ha activado
        if (showMinimap) {
            drawMinimap(canvas)
        }

        // Dibuja el icono del botón del minimapa
        canvas.drawBitmap(minimapButtonIcon, null, hudButtonRect, paint)

        // Dibuja el icono del HUD (vida y escudo)
        canvas.drawBitmap(lifeButtonIcon, null, hudLifeButtonRect, paint)

        if (showHudStatus) {
            val barWidth = 400f     // Ancho fijo
            val barHeight = 30f
            val spacing = 20f

            val shipLife = ship.getHealth()
            val shipShield = ship.getShield()
            val maxLife = ship.getMaxHealth()
            val maxShield = ship.getMaxShield()

            val baseX = (width - barWidth) / 2f
            var baseY = height * 5f / 6f

            // Barra de vida (verde)
            paint.color = Color.DKGRAY
            canvas.drawRect(baseX, baseY, baseX + barWidth, baseY + barHeight, paint)
            paint.color = Color.GREEN
            canvas.drawRect(baseX, baseY, baseX + barWidth * shipLife / maxLife, baseY + barHeight, paint)
            paint.color = Color.BLACK
            paint.textSize = 26f
            canvas.drawText("Vida: ${shipLife.toInt()}", baseX + 10f, baseY + 24f, paint)

            // Barra de escudo (azul)
            baseY += barHeight + spacing
            paint.color = Color.DKGRAY
            canvas.drawRect(baseX, baseY, baseX + barWidth, baseY + barHeight, paint)
            paint.color = Color.CYAN
            canvas.drawRect(baseX, baseY, baseX + barWidth * shipShield / maxShield, baseY + barHeight, paint)
            paint.color = Color.BLACK
            canvas.drawText("Escudo: ${shipShield.toInt()}", baseX + 10f, baseY + 24f, paint)
        }

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

        // Dibuja la nave
        val minimapShipX = minimapX + shipMapX * scaleX
        val minimapShipY = minimapY + shipMapY * scaleY
        val minimapShipSize = 5f
        paint.color = Color.GREEN
        canvas.drawCircle(minimapShipX, minimapShipY, minimapShipSize, paint)

        // Dibuja enemigos
        paint.color = Color.RED
        for (enemy in enemies) {
            val ex = minimapX + enemy.getX() * scaleX
            val ey = minimapY + enemy.getY() * scaleY
            canvas.drawCircle(ex, ey, minimapShipSize, paint)
        }


    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Revisar si se toca el botón del HUD
                if (hudButtonRect.contains(event.x, event.y)) {
                    showMinimap = !showMinimap
                    invalidate()
                } else if (hudLifeButtonRect.contains(event.x, event.y)) {
                    showHudStatus = !showHudStatus
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