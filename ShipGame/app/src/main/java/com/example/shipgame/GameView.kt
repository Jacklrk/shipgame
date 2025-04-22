package com.example.shipgame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import com.example.shipgame.entities.Ship
import com.example.shipgame.entities.Enemy
import com.example.shipgame.entities.Projectile
import com.example.shipgame.entities.Entity



class GameView(context: Context) : View(context) {

    private val ship = Ship(x,y,vida = 80000, escudo = 100000,maxvida = 80000,maxescudo = 100000,0, velocidad = 8f)
    private val paint = Paint()
    private var shipBitmap: Bitmap

    private val shipSize = 70

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
    private val enemySize = 150

    // HUD: Minimapa y su visibilidad
    private var showMinimap = false
    private val minimapSize = 400 // Tamaño del minimapa en píxeles

    // Rectángulo para el botón del HUD
    private val hudButtonRect = RectF(50f, 50f, 150f, 150f)

    // Imagen del icono del minimapa
    private val minimapButtonIcon: Bitmap
    private val hudLifeButtonRect = RectF(50f, 160f, 150f, 260f)  // Debajo del minimapa
    private lateinit var lifeButtonIcon: Bitmap
    // Botón de disparo (HUD)
    private lateinit var shootButtonIcon: Bitmap
    private lateinit var shootButtonRect: RectF

    // HUD de vida/escudo
    private var showHudStatus = false

    //Marcador enemigos
    private val enemyMarkerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.marcador_ene)
    private var selectedEnemy: Entity? = null

    //Generador de enemigos
    private val spawnInterval = 600  // Cada 600 frames (~10 segundos si estás a 60 fps)

    //Proyectiles enemigos
    private val projectileBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.lordakium_shot)
    private val projectiles = mutableListOf<Projectile>()

    private var frameCount = 0

    //Ataque nave
    private val playerProjectileBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.laser1)
    private var isAttacking = false



    init {
        val res = resources
        // Carga del botón de disparo
        shootButtonIcon = BitmapFactory.decodeResource(res, R.drawable.disparar)

        // Carga la imagen del botón minimapa
        minimapButtonIcon = BitmapFactory.decodeResource(res, R.drawable.minimap)

        // Carga la imagen del botón de vida/escudo
        lifeButtonIcon = BitmapFactory.decodeResource(res, R.drawable.lifeship)

        // Genera 32 ángulos espaciados cada 11.25 grados y redondeados a enteros
        val angles = List(32) { i -> (i * 11.25).roundToInt() }

        // Carga los sprites con nombres saturn_[ángulo], como saturn_0, saturn_11, ..., saturn_349
        shipSprites = angles.associateWith { angle ->
            val resId = res.getIdentifier("saturn_$angle", "drawable", context.packageName)
            Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, resId), shipSize, shipSize, true)
        }

        // Valor inicial (mirando al este, 90
        shipBitmap = shipSprites[90]!!


        // Carga el mapa de fondo
        backgroundMap = BitmapFactory.decodeResource(res, R.drawable.mapa)
    }

    init {
        val rng = java.util.Random()
        repeat(5) {
            val x = rng.nextInt(backgroundMap.width).toFloat()
            val y = rng.nextInt(backgroundMap.height).toFloat()
            enemies.add(Enemy(x, y, 100000, 150, 0, 100000, 100))
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val buttonSize = 250f
        shootButtonRect = RectF(
            w - buttonSize - 50f,
            h - buttonSize - 50f,
            w - 50f,
            h - 50f
        )
    }


    override fun onDraw(canvas: Canvas) {
        frameCount++
        super.onDraw(canvas)
        //VISTA HACIA ENEMIGO ATACANDO
        if (isAttacking && selectedEnemy != null) {
            val dx = selectedEnemy!!.getX() - ship.getX()
            val dy = selectedEnemy!!.getY() - ship.getY()

            val angle = (Math.toDegrees(atan2(dx, -dy).toDouble()) + 360).toInt() % 360
            val directions = List(32) { i -> (i * 11.25).roundToInt() }
            val closestAngle = directions.minByOrNull { abs(it - angle) } ?: 0
            shipBitmap = shipSprites[closestAngle] ?: shipBitmap
        }

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

            // Actualiza también la posición lógica del objeto Ship
            ship.moveBy(moveDx, moveDy)
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



            //MARCADOR enemigo seleccionado
            if (enemy == selectedEnemy) {
                val markerSize = 70
                val markerX = enemyScreenX - markerSize / 2
                val markerY = enemyScreenY - enemySize / 2 - markerSize + 100

                // Dibuja marcador
                val markerRect = RectF(
                    markerX,
                    markerY,
                    markerX + markerSize,
                    markerY + markerSize
                )
                canvas.drawBitmap(enemyMarkerBitmap, null, markerRect, paint)

                // Obtener valores del enemigo
                val hp = enemy.getHealth().toFloat()
                val maxHP = enemy.getMaxHealth().toFloat()
                val shield = enemy.getShield().toFloat()
                val maxShield = enemy.getMaxShield().toFloat()

                // ----- Barras de vida y escudo dinámicas -----
                val barMaxWidth = 80f
                val barHeight = 10f
                val barX = enemyScreenX - barMaxWidth / 2
                var barY = markerY + markerSize + 5f

                // Vida (verde)
                paint.color = Color.DKGRAY
                canvas.drawRect(barX, barY, barX + barMaxWidth, barY + barHeight, paint)
                paint.color = Color.GREEN
                val lifeRatio = (hp / maxHP).coerceIn(0f, 1f)
                canvas.drawRect(barX, barY, barX + barMaxWidth * lifeRatio, barY + barHeight, paint)

                // Escudo (azul)
                barY += barHeight + 4f
                paint.color = Color.DKGRAY
                canvas.drawRect(barX, barY, barX + barMaxWidth, barY + barHeight, paint)
                paint.color = Color.CYAN
                val shieldRatio = (shield / maxShield).coerceIn(0f, 1f)
                canvas.drawRect(barX, barY, barX + barMaxWidth * shieldRatio, barY + barHeight, paint)
            }


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

            val shootRange = 1000f
            val timeBetweenShots = 60  // frames (~1s a 60fps)
            if (distanceenemy < shootRange && frameCount % timeBetweenShots == 0) {
                enemyShoot(enemy.getX(), enemy.getY())
            }

            // 💥 ATAQUE DE LA NAVE
            if (isAttacking && frameCount % 20 == 0 && selectedEnemy != null) {
                val projectile = Projectile(
                    x = ship.getX(),
                    y = ship.getY(),
                    target = selectedEnemy!!,
                    disparadoPorJugador = true,
                    speed = 20f
                )
                projectiles.add(projectile)
            }

        }
        invalidate()

        //PROYECTILES enemigos

        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val proj = iterator.next()
            if (!proj.active) {
                iterator.remove()
                continue
            }

            val dx = proj.target.getX() - proj.x
            val dy = proj.target.getY() - proj.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance < 10f) {
                proj.target.takeDamage(500)
                // Si el objetivo muere
                if (proj.target.getHealth() <= 0) {
                    if (proj.target == selectedEnemy) {
                        selectedEnemy = null
                        isAttacking = false
                    }
                    if (proj.target is Enemy) {
                        enemies.remove(proj.target)
                    }
                }
                iterator.remove()
                continue
            }

            val moveX = proj.speed * (dx / distance)
            val moveY = proj.speed * (dy / distance)
            proj.x += moveX
            proj.y += moveY

            // 🧭 Posición en pantalla (ajustada con centro real de nave)
            val projScreenX = (proj.x - ship.getX()) + centerX
            val projScreenY = (proj.y - ship.getY()) + centerY

            val angleRad = atan2(dy, dx)
            val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

            canvas.save()
            canvas.rotate(angleDeg, projScreenX, projScreenY)

            val laserWidth = 64f
            val laserHeight = 12f
            val rect = RectF(
                projScreenX - laserWidth / 2,
                projScreenY - laserHeight / 2,
                projScreenX + laserWidth / 2,
                projScreenY + laserHeight / 2
            )

            val bitmapToUse = if (proj.disparadoPorJugador) playerProjectileBitmap else projectileBitmap
            canvas.drawBitmap(bitmapToUse, null, rect, paint)

            canvas.restore()
        }

        // 🌟 Generar enemigo cada cierto número de frames
        if (frameCount % spawnInterval == 0 && enemies.size < 20) {
            val rng = java.util.Random()
            var newX: Float
            var newY: Float
            var safe = false

            // Asegurar que no spawneen justo encima de la nave
            while (!safe) {
                newX = rng.nextInt(backgroundMap.width).toFloat()
                newY = rng.nextInt(backgroundMap.height).toFloat()
                val dx = newX - ship.getX()
                val dy = newY - ship.getY()
                val dist = sqrt(dx * dx + dy * dy)

                if (dist > 800f) {  // Distancia mínima al jugador para evitar spawn sobre él
                    enemies.add(Enemy(newX, newY, 100, 100, 0, 100, 100))
                    safe = true
                }
            }
        }

        // Mostrar minimapa si se ha activado
        if (showMinimap) {
            drawMinimap(canvas)
        }

        // Dibuja el icono del botón del minimapa
        canvas.drawBitmap(minimapButtonIcon, null, hudButtonRect, paint)

        // Dibuja el icono del HUD (vida y escudo)
        canvas.drawBitmap(lifeButtonIcon, null, hudLifeButtonRect, paint)

        // Dibuja el botón de disparo
        canvas.drawBitmap(shootButtonIcon, null, shootButtonRect, paint)


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
    //*******************************
    //PROYECTILES
    private fun enemyShoot(enemyX: Float, enemyY: Float) {
        val projectile = Projectile(
            x = enemyX,
            y = enemyY,
            speed = 15f,
            target = ship,
            disparadoPorJugador = false
        )
        projectiles.add(projectile)
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
                // Verifica primero si se tocó un botón
                if (hudButtonRect.contains(event.x, event.y)) {
                    showMinimap = !showMinimap
                    invalidate()
                    return true
                } else if (hudLifeButtonRect.contains(event.x, event.y)) {
                    showHudStatus = !showHudStatus
                    invalidate()
                    return true
                } else if (shootButtonRect.contains(event.x, event.y)) {
                    isAttacking = !isAttacking
                    invalidate()
                    return true
                }

                // ✅ Detectar toque sobre enemigos (pero no detener el movimiento)
                for (enemy in enemies.reversed()) {
                    val enemyScreenX = (enemy.getX() - ship.getX()) + width / 2
                    val enemyScreenY = (enemy.getY() - ship.getY()) + height / 2
                    val touchRadius = enemySize / 2

                    val dx = event.x - enemyScreenX
                    val dy = event.y - enemyScreenY

                    if (sqrt(dx * dx + dy * dy) <= touchRadius) {
                        selectedEnemy = enemy
                        invalidate()
                        // ❗No returns here — sigue evaluando el movimiento
                        break
                    }
                }

                // ✅ Movimiento de la nave
                val dx = event.x - width / 2
                val dy = event.y - height / 2

                val angle = (Math.toDegrees(atan2(dx, -dy).toDouble()) + 360).toInt() % 360
                val directions = List(32) { i -> (i * 11.25).roundToInt() }
                val closestAngle = directions.minByOrNull { abs(it - angle) } ?: 0
                shipBitmap = shipSprites[closestAngle] ?: shipBitmap

                val speed = ship.getVelocidad()
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