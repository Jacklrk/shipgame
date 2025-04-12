package com.example.shipgame.entities

class BossEnemy(
    x: Float,
    y: Float,
    health: Int = 200, // Más vida
    shield: Int = 100, // Más escudo
    storage: Int = 0 // Almacenamiento por defecto
) // Enemy(x, y, health, shield, storage)
/*{

    // Lógica del enemigo de tipo jefe
    fun specialAttack(player: Ship) {
        val damage = 50 // Daño más fuerte
        player.takeDamage(damage)
    }

    override fun takeDamage(damage: Int) {
        // Lógica personalizada de daño si es necesario
        super.takeDamage(damage)
    }
}*/
