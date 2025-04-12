package com.example.shipgame.entities

class Ship : Entity(
    x = 0.0F,
    y = 0.0F,
    vida = 160000,
    escudo = 256000,
    maxvida = 360000,
    maxescudo = 560000,
    almacenamiento = 0,
    velocidad = 10f
)


/*override fun takeDamage(damage: Int) {
    // Lógica personalizada de daño si es necesario
    super.takeDamage(damage)
}*/

