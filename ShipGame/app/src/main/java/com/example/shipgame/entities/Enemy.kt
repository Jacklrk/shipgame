package com.example.shipgame.entities

/* Enemy.kt */
class Enemy(
    val name: String = "-=Lordakium=-",           // ← NUEVO
    x: Float,
    y: Float,
    vida: Int,
    escudo: Int,
    almacenamiento: Int,
    maxvida: Int,
    maxescudo: Int
) : Entity(x, y, vida, escudo, almacenamiento, maxvida, maxescudo, velocidad = 2f) {

    fun attack(ship: Ship) { ship.takeDamage(50) }

    override fun takeDamage(damage: Int) { super.takeDamage(damage) }
}


