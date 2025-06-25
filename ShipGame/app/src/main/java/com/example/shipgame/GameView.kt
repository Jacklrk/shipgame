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
import java.util.ArrayDeque
import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.example.shipgame.entities.Structure



class GameView(context: Context) : View(context) {
    // Dimensiones lógicas del universo (pixeles del bitmap de fondo)
    private val WORLD_WIDTH  = 5_000f
    private val WORLD_HEIGHT = 5_000f
    //NAVE PROPIEDADES
    private val spriteAngles = List(32) { i -> (i * 11.25).roundToInt() }
    private val ship = Ship(x,y,vida = 80000, escudo = 100000,maxvida = 80000,maxescudo = 100000,0, velocidad = 3f)
    private val paint = Paint()
    private var shipBitmap: Bitmap
    private val shipSize = 150
    private val shipSprites: Map<Int, Bitmap>  // Sprites por ángulo
    private val backgroundMap: Bitmap

    private var shipMapX = 0f
    private var shipMapY = 0f

    private var isMoving = false
    private var moveDx = 0f
    private var moveDy = 0f

    /* ------------- Star field ----------------------------- */
    private data class Star(val x: Float, val y: Float, val r: Float, val alpha: Int)

    /** nº total de estrellas  */
    private val STAR_COUNT = 250
    /** factor de parallax: 0 = pegadas al fondo, 1 = pegadas a la cámara */
    private val STAR_PARALLAX = 0.4f
    /** lista inmutable generada una vez */
    private val stars: List<Star>

    //ESTRUCTURAS
    private lateinit var structures: List<Structure>

    // ─── Joystick MOVER NAVE SIN PRESIONAR PANTALLA
    private val joystickBitmap = BitmapFactory.decodeResource(
        resources, R.drawable.mover_nave)       // 768×768 px
    private val joyRadiusPx = 140f              // radio del círculo visible
    private val joyTouchRadius = 220f           // radio invisible: área de arrastre
    private var joyCenterX = 0f                 // se calcula en onSizeChanged
    private var joyCenterY = 0f
    private var touchingJoystick = false

    // ─── Auto-apuntado NAVE───────────────────────────────────────────
    private val autoAimIcon = BitmapFactory.decodeResource(
        resources, R.drawable.aapuntado)   // PNG que adjuntaste
    private lateinit var autoAimRect: RectF    // se calcula en onSizeChanged
    private var autoAimOn = false              // estado actual


    //*************** Enemigo
    private val enemies = mutableListOf<Enemy>()
    private val enemyBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.lordakium)
    private val enemySize = 150
    private val fleeTargets = mutableMapOf<Enemy, PointF>()   // destino de huida

    // HUD: Minimapa y su visibilidad
    private var showMinimap = false
    private val minimapSize = 400 // Tamaño del minimapa en píxeles

    // Rectángulo para el botón del HUD
    private val hudButtonRect = RectF(50f, 50f, 150f, 150f)

    // ---- Salir / cuenta regresiva -VUELA A MAINACTIVITY
    private val exitIcon = BitmapFactory.decodeResource(resources, R.drawable.apagar_btn)
    private lateinit var exitRect: RectF

    private val exitHandler = Handler(Looper.getMainLooper())
    private var exitRunnable: Runnable? = null
    private var exitEndTime: Long = 0          // millis cuando termina
    private var exitCountdownActive = false


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
        BitmapFactory.decodeResource(resources, R.drawable.laser2)
    private var isAttacking = false

    //EXPLOSIONES
    // --- explosiones ---
    private val atlas = AtlasManager.bitmap()
    private val explosionRects = AtlasManager.frames("explosion")
    private val activeExplosions = mutableListOf<ExplosionInstance>()
    private val explosionPool = ArrayDeque<FrameAnimation>()

    private data class ExplosionInstance(
        val anim: FrameAnimation,
        var x: Float,
        var y: Float
    )

    private fun spawnExplosion(x: Float, y: Float) {
        val anim = explosionPool.poll() ?: FrameAnimation(explosionRects, 15)
        anim.reset()
        activeExplosions += ExplosionInstance(anim, x, y)
    }



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
        val rng = java.util.Random()

        // estrellas repartidas por todo el universo lógico
        stars = List(STAR_COUNT) {
            Star(
                x = rng.nextFloat() * WORLD_WIDTH,
                y = rng.nextFloat() * WORLD_HEIGHT,
                r = rng.nextFloat() * 2f + 0.5f,          // 0.5–2.5 px
                alpha = (rng.nextInt(100) + 155)          // brillo 155-255
            )
        }

        val sSize = 280        // lado en pantalla
        fun loadStruct(id: Int) =
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(res, id),
                sSize, sSize, true)

        // bitmaps
        val baseVru   = loadStruct(R.drawable.base1)   // usa el nombre que le pongas
        val baseMmo   = loadStruct(R.drawable.base2)
        val hangarBox = loadStruct(R.drawable.base3)
        val satelite  = loadStruct(R.drawable.base4)

        // lista global
        structures = listOf(
            Structure(baseVru,   1000f, 1500f, 1000),
            Structure(baseMmo,   500f, 1500f, sSize),
            Structure(hangarBox, 1500f,  1500f, sSize),
            Structure(satelite,  1555f, 1750f, sSize)
        )
    }

    init {
        val rng = java.util.Random()
        repeat(5) {
            val x = rng.nextInt(backgroundMap.width).toFloat()
            val y = rng.nextInt(backgroundMap.height).toFloat()
            enemies.add(Enemy(x, y, 10000, 15000, 0, 10000, 15000))
        }
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //btn joystick
        val margin = 32 * resources.displayMetrics.density
        joyCenterX = margin + joyRadiusPx
        joyCenterY = h - margin - joyRadiusPx
        //btn disparo
        val buttonSize = 250f
        shootButtonRect = RectF(
            w - buttonSize - 50f,
            h - buttonSize - 50f,
            w - 50f,
            h - 50f
        )
        //btn autoaim
        // --- botón auto-aim (tamaño 56 dp) ---
        val dp      = resources.displayMetrics.density
        val sizePx  = 56 * dp
        val marginaa  = 16 * dp                // separación vertical

        val centerX = shootButtonRect.centerX()
        val bottomY = shootButtonRect.top - marginaa

        autoAimRect = RectF(
            centerX - sizePx / 2,
            bottomY - sizePx,
            centerX + sizePx / 2,
            bottomY
        )
        //BTN SALIR
        val size  = 56 * dp            // 56 dp lado
        val marginsalida= 12 * dp

        exitRect = RectF(
            width - marginsalida - size,
            marginsalida,
            width - marginsalida,
            marginsalida + size
        )

    }


    override fun onDraw(canvas: Canvas) {
        frameCount++
        super.onDraw(canvas)
        if (autoAimOn && (selectedEnemy == null || selectedEnemy!!.getHealth() <= 0)) {
            selectedEnemy = pickNearestEnemy()
        }

        //VISTA HACIA ENEMIGO ATACANDO
        // 1. Actualiza la orientación de la nave (solo si está atacando y hay objetivo)
        if (isAttacking && selectedEnemy != null && selectedEnemy!!.getHealth() > 0) {
            val dx = selectedEnemy!!.getX() - ship.getX()
            val dy = selectedEnemy!!.getY() - ship.getY()

            val angle = (Math.toDegrees(atan2(dx, -dy).toDouble()) + 360).toInt() % 360
            val directions = List(32) { i -> (i * 11.25).roundToInt() }
            val closestAngle = directions.minByOrNull { abs(it - angle) } ?: 0
            shipBitmap = shipSprites[closestAngle] ?: shipBitmap

            // Disparo visual doble o simple
            if (frameCount % 20 == 0) {
                val shipCenterX = ship.getX() + shipSize / 2f
                val shipCenterY = ship.getY() + shipSize / 2f

                val angleRad = atan2(dy, dx)
                val spread = 25f
                val offsetX = cos(angleRad + Math.PI / 2).toFloat() * spread
                val offsetY = sin(angleRad + Math.PI / 2).toFloat() * spread

                if ((frameCount / 20) % 2 == 0) {
                    projectiles.add(Projectile(shipCenterX + offsetX, shipCenterY + offsetY, 20f,selectedEnemy!!, true))
                    projectiles.add(Projectile(shipCenterX - offsetX, shipCenterY - offsetY, 20f,selectedEnemy!!, true))
                } else {
                    projectiles.add(Projectile(shipCenterX, shipCenterY, 20f,selectedEnemy!!, true))
                }
            }
        } else if (selectedEnemy != null && selectedEnemy!!.getHealth() <= 0) {
            selectedEnemy = null
            isAttacking = false
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
        //bases
        for (st in structures) {
            st.draw(canvas, paint, shipMapX, shipMapY, centerX, centerY)
        }


        canvas.drawBitmap(shipBitmap, centerX.toFloat(), centerY.toFloat(), paint)
        // Movimiento continuo del jugador (si se está moviendo)
        if (isMoving) {
            shipMapX += moveDx
            shipMapY += moveDy

            // Actualiza también la posición lógica del objeto Ship
            ship.moveBy(moveDx, moveDy)
        }
        // --- límites del universo ---
        // --- movimiento continuo del jugador ---
        if (isMoving) {
            /* 1️⃣  Calcula la candidata SIN tocar aún las coords del ship */
            val nextX = (shipMapX + moveDx).coerceIn(0f, WORLD_WIDTH  - shipSize)
            val nextY = (shipMapY + moveDy).coerceIn(0f, WORLD_HEIGHT - shipSize)

            /* 2️⃣  Delta realmente permitido tras hacer clamp */
            val dxClamped = nextX - shipMapX
            val dyClamped = nextY - shipMapY

            /* 3️⃣  Actualiza las DOS fuentes de verdad con el mismo delta */
            shipMapX = nextX
            shipMapY = nextY
            ship.moveBy(dxClamped, dyClamped)
        }

        //******************** Enemigo
        //*********** Dibujar enemigos
        for (enemy in enemies) {

            /* ────────────────────────────
             *  A) CÁLCULOS DE PANTALLA
             * ──────────────────────────── */
            val enemyScreenX = (enemy.getX() - shipMapX) + centerX
            val enemyScreenY = (enemy.getY() - shipMapY) + centerY
            val verticalOffset = 7f

            // Dibujo del sprite enemigo
            val enemyRect = RectF(
                enemyScreenX - enemySize / 2,
                enemyScreenY - enemySize / 2 + verticalOffset,
                enemyScreenX + enemySize / 2,
                enemyScreenY + enemySize / 2 + verticalOffset
            )
            canvas.drawBitmap(enemyBitmap, null, enemyRect, paint)

            /* ────────────────────────────
             *  B) MARCADOR si es objetivo
             * ──────────────────────────── */
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

            /* ────────────────────────────
             *  C) ZONA DE NO-ATAQUES
             * ──────────────────────────── */
            val playerIsTargeting = isAttacking && selectedEnemy == enemy
            val insideSafeZone = structures.any { it.contains(enemy.getX(), enemy.getY()) }

            // 1) Si entra y no es el objetivo del jugador ⇒ asigna destino de huida
            if (insideSafeZone && !playerIsTargeting) {
                fleeTargets.getOrPut(enemy) { randomPointOutsideSafeZones() }
            } else {
                // 2) Fuera de la zona o sí es el objetivo ⇒ cancela huida
                fleeTargets.remove(enemy)
            }

            /* ────────────────────────────
             *  D) MOVIMIENTO
             * ──────────────────────────── */
            val enemySpeed     = 2f
            val stopRange      = 175f          // para “llegué” ya sea a nave o huida
            val fleeTarget     = fleeTargets[enemy]

            if (fleeTarget != null) {
                // — modo HUÍDA —
                val dx = fleeTarget.x - enemy.getX()
                val dy = fleeTarget.y - enemy.getY()
                val d  = sqrt(dx*dx + dy*dy).coerceAtLeast(1f)

                enemy.moveBy(enemySpeed * dx / d, enemySpeed * dy / d)

                // ¿Llegó?  —> borra destino
                if (d < stopRange) fleeTargets.remove(enemy)
                continue                 // no persigue ni dispara este frame
            }

            // — modo PERSECUCIÓN NORMAL —
            val dxToPlayer = shipMapX - enemy.getX()
            val dyToPlayer = shipMapY - enemy.getY()
            val distToPlayer = sqrt(dxToPlayer*dxToPlayer + dyToPlayer*dyToPlayer)

            val detectionRange = 1_100f
            if (distToPlayer < detectionRange && distToPlayer > stopRange) {
                enemy.moveBy(enemySpeed * dxToPlayer / distToPlayer,
                    enemySpeed * dyToPlayer / distToPlayer)
            }

            /* ────────────────────────────
             *  E) DISPARO
             * ──────────────────────────── */
            val shootRange        = 500f
            val timeBetweenShots  = 60  // 1 s ≈ 60 fps
            if (distToPlayer < shootRange && frameCount % timeBetweenShots == 0) {
                enemyShoot(enemy.getX(), enemy.getY())
            }
        }


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
                        spawnExplosion(proj.target.getX(), proj.target.getY())
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
                    enemies.add(Enemy(newX, newY, 10000, 15000, 0, 10000, 15000))
                    safe = true
                }
            }
        }
        paint.style = Paint.Style.FILL
        for (star in stars) {

            // parallax: estrellas se desplazan un % de la cámara
            val sx = (star.x - shipMapX * STAR_PARALLAX) + centerX
            val sy = (star.y - shipMapY * STAR_PARALLAX) + centerY

            // descarta las que queden fuera del viewport
            if (sx < -5 || sx > width + 5 || sy < -5 || sy > height + 5) continue

            paint.color = Color.argb(star.alpha, 255, 255, 255)
            canvas.drawCircle(sx, sy, star.r, paint)
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

        //Dibuja el boton del joystick
        // escala a 2 × radio
        val joyDst = RectF(
            joyCenterX - joyRadiusPx, joyCenterY - joyRadiusPx,
            joyCenterX + joyRadiusPx, joyCenterY + joyRadiusPx
        )
        canvas.drawBitmap(joystickBitmap, null, joyDst, paint)

        //Dibuja el autoaim
        canvas.drawBitmap(autoAimIcon, null, autoAimRect, paint)

        //Dibuja el botón de salir
        // *********Botón salir – solo si no está el minimapa
        if (!showMinimap) {
            canvas.drawBitmap(exitIcon, null, exitRect, paint)
        }
        // ********Texto de cuenta atrás
        if (exitCountdownActive) {
            val secs = ((exitEndTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            paint.color = Color.WHITE
            paint.textSize = 54f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Saliendo en $secs", width / 2f, height / 2f, paint)
        }


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
        //ZONA DE NO ATAQUES
        val inSafeZone = structures.any { it.contains(ship.getX(), ship.getY()) }

        if (inSafeZone) {
            paint.color = Color.YELLOW
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 46f
            canvas.drawText("Zona de no ataques", width / 2f, 80f, paint)
        }

        //EXPLOSIONES
        // --- explosiones ---
        val dt = 1f / 60f   // o usa tu delta real
        for (exp in activeExplosions.toList()) {
            exp.anim.update(dt)
            val screenX = (exp.x - ship.getX()) + centerX
            val screenY = (exp.y - ship.getY()) + centerY
            exp.anim.draw(canvas, atlas, screenX, screenY)

            if (exp.anim.finished) {
                activeExplosions.remove(exp)
                explosionPool.offer(exp.anim)
            }
        }

        invalidate()
    }
    //*******************************
    //PROYECTILES ENEMIGOS
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

    private fun randomPointOutsideSafeZones(): PointF {
        val rng = java.util.Random()
        var x: Float; var y: Float
        do {
            x = rng.nextFloat() * WORLD_WIDTH
            y = rng.nextFloat() * WORLD_HEIGHT
        } while (structures.any { it.contains(x, y) })
        return PointF(x, y)
    }

    //JOYSTICK LOGICA

    private fun setSpriteForVector(dx: Float, dy: Float) {
        // Si estamos atacando y el enemigo sigue vivo, NO cambiamos orientación
        if (isAttacking && selectedEnemy?.getHealth() ?: 0 > 0) return

        val angle = ((Math.toDegrees(atan2(dx, -dy).toDouble()) + 360) % 360).toInt()
        val closest = spriteAngles.minByOrNull { abs(it - angle) } ?: 0
        shipBitmap = shipSprites[closest] ?: shipBitmap
    }
    private fun handleJoystick(x: Float, y: Float) {
        val dx = x - joyCenterX
        val dy = y - joyCenterY
        val dist = hypot(dx, dy).coerceAtLeast(1f)

        val speed = ship.getVelocidad()
        moveDx = speed * (dx / dist)
        moveDy = speed * (dy / dist)
        isMoving = true

        setSpriteForVector(dx, dy)
    }

    private fun handleTapMove(x: Float, y: Float) {
        val dx = x - width / 2f
        val dy = y - height / 2f
        val dist = hypot(dx, dy).coerceAtLeast(1f)

        val speed = ship.getVelocidad()
        moveDx = speed * (dx / dist)
        moveDy = speed * (dy / dist)
        isMoving = true

        setSpriteForVector(dx, dy)
    }

    //AUTOAIM LOGICA
    private fun pickNearestEnemy(): Enemy? {
        var closest: Enemy? = null
        var minDist2 = Float.MAX_VALUE

        for (e in enemies) {
            if (e.getHealth() <= 0) continue
            val dx = e.getX() - ship.getX()
            val dy = e.getY() - ship.getY()
            val d2 = dx*dx + dy*dy
            if (d2 < minDist2) { minDist2 = d2; closest = e }
        }
        return closest
    }


    //MINIMAPA LOGICA
    private fun drawMinimap(canvas: Canvas) {
        val mscaleX = minimapSize / WORLD_WIDTH
        val mscaleY = minimapSize / WORLD_HEIGHT

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
        // Coordenadas de la nave
        val coordText = "X ${shipMapX.toInt()}  Y ${shipMapY.toInt()}"
        paint.textSize = 30f
        paint.color = Color.WHITE
        canvas.drawText(coordText, minimapX, minimapY + minimapSize + 32f, paint)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = event.actionIndex
                val x = event.getX(idx)
                val y = event.getY(idx)

                // 🕹 ¿tocó joystick?
                if (hypot(x - joyCenterX, y - joyCenterY) <= joyTouchRadius) {
                    touchingJoystick = true
                    handleJoystick(x, y)
                    return true
                }
                // botones HUD (…igual que antes…)
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
                } else if (exitRect.contains(event.x, event.y) && !showMinimap) {
                    if (exitCountdownActive) {            // segundo toque ⇒ cancelar
                        exitHandler.removeCallbacks(exitRunnable!!)
                        exitCountdownActive = false
                    } else {                              // primer toque ⇒ iniciar
                        exitCountdownActive = true
                        exitEndTime = System.currentTimeMillis() + 5000
                        exitRunnable = Runnable {
                            exitCountdownActive = false
                            (context as? Activity)?.finish()   // vuelve al Hangar
                        }
                        exitHandler.postDelayed(exitRunnable!!, 5000)
                    }
                    invalidate()
                    return true
                } else if (autoAimRect.contains(event.x, event.y)) {
                    autoAimOn = !autoAimOn

                    if (autoAimOn) {
                        selectedEnemy = pickNearestEnemy()   // ← solo aquí
                        //isAttacking = true        // activa disparo si lo deseas
                    } else {
                        selectedEnemy = null
                    }
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

                // movimiento por pantalla (igual que antes)
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
                handleTapMove(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchingJoystick) {
                    for (i in 0 until event.pointerCount) {
                        val x = event.getX(i); val y = event.getY(i)
                        if (hypot(x - joyCenterX, y - joyCenterY) <= joyTouchRadius * 2) {
                            handleJoystick(x, y); break
                        }
                    }
                } else {
                    handleTapMove(event.x, event.y)
                }
                invalidate()          // fuerza repintado con nuevo sprite
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                touchingJoystick = false
                isMoving = false
            }
        }
        return true
    }

}