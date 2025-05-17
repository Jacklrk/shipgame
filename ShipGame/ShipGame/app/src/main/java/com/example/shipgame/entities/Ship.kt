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
) : Entity(x, y, vida, escudo, almacenamiento, maxvida, maxescudo, velocidad){
    override fun takeDamage(damage: Int) {
        // Efecto visual o lógica especial
        super.takeDamage(damage)
    }
}

