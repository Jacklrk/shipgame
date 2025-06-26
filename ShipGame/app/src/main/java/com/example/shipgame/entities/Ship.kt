package com.example.shipgame.entities

class Ship(
    x: Float,
    y: Float,
    vida: Int,
    escudo: Int,
    maxvida: Int,
    maxescudo: Int,
    almacenamiento: Int,
    velocidad: Float
) : Entity(x, y, vida, escudo, almacenamiento, maxvida, maxescudo, velocidad) {

    /* ─── control de regeneración ─── */
    var lastHitTime   = 0L      // ms del último daño recibido
    var lastShotTime  = 0L      // ms del último disparo propio
    var shieldRegenActive = false
    var lifeRegenActive   = false

    /* ─── cuando recibe daño ─── */
    override fun takeDamage(damage: Int) {
        super.takeDamage(damage)            // resta vida/escudo
        lastHitTime = System.currentTimeMillis()
        shieldRegenActive = false
        lifeRegenActive   = false
    }

    /* ─── utilidades para curar ─── */
    fun addShield(points: Int) {
        setShield((getShield() + points).coerceAtMost(getMaxShield()))
    }

    fun addHealth(points: Int) {
        setHealth((getHealth() + points).coerceAtMost(getMaxHealth()))
    }
}
