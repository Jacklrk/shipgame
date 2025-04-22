package com.example.shipgame.entities

class Enemy(
    x: Float,
    y: Float,
    vida: Int,
    escudo: Int,
    almacenamiento: Int,
    maxvida: Int,
    maxescudo: Int
) : Entity(x, y, vida, escudo, almacenamiento, maxvida, maxescudo, velocidad=2f){
    fun attack(ship: Ship) {
        val damage = 50
        ship.takeDamage(damage)
    }

    override fun takeDamage(damage: Int) {
        super.takeDamage(damage)
        // Aquí puedes agregar efectos o animaciones si es golpeado
    }
}


