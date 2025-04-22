package com.example.shipgame.entities

open class Entity(
    private var x: Float,
    private var y: Float,
    private var vida: Int,
    private var escudo: Int,
    private var almacenamiento: Int,
    private var maxvida: Int,
    private var maxescudo: Int,
    private var velocidad: Float
) {
    fun getX() = x
    fun getY() = y
    fun getHealth() = vida
    fun getShield() = escudo
    fun getMaxHealth() = maxvida
    fun getMaxShield() = maxescudo
    fun getAlmacenamiento() = almacenamiento
    fun getVelocidad() = velocidad
    fun moveBy(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun setHealth(value: Int) {
        vida = value
    }

    fun setShield(value: Int) {
        escudo = value
    }

    fun isAlive(): Boolean {
        return vida > 0
    }
    fun damage(amount: Int) {
        if (escudo > 0) {
            val absorbed = minOf(amount, escudo)
            escudo -= absorbed
            val rest = amount - absorbed
            vida -= rest
        } else {
            vida -= amount
        }

        if (vida <= 0) {
            // puedes marcarlo como eliminado o quitarlo de la lista
        }
    }
    open fun takeDamage(damage: Int) {
        if (escudo > 0) {
            val absorbed = minOf(damage, escudo)
            escudo -= absorbed
            val remaining = damage - absorbed
            vida -= remaining
        } else {
            vida -= damage
        }
    }


}

