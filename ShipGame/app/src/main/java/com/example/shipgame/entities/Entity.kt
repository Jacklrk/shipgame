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
}

/*{
   open fun takeDamage(damage: Int) {
       // Primero se resta del escudo si hay
       if (shield > 0) {
           val shieldDamage = minOf(damage, shield)
           shield -= shieldDamage
           damage -= shieldDamage
       }
       // Después, se resta de los puntos de vida
       if (damage > 0) {
           health -= damage
       }
   }

   open fun isAlive(): Boolean {
       return health > 0
   }

   // Método para mover la entidad
   open fun move(dx: Float, dy: Float) {
       x += dx
       y += dy
   }
}*/