package com.example.shipgame.entities

class Enemy(
    x: Float,
    y: Float,
    vida: Int,
    escudo: Int,
    almacenamiento: Int,
    maxvida: Int,
    maxescudo: Int
) : Entity(x, y, vida, escudo, almacenamiento, maxvida, maxescudo, velocidad=5f)


/*{

   // Aquí puedes agregar más comportamientos específicos para los enemigos
   fun attack(player: Ship) {
       val damage = 10 // Ejemplo de daño
       player.takeDamage(damage)
   }

   override fun takeDamage(damage: Int) {
       // Lógica personalizada de daño si es necesario
       super.takeDamage(damage)
   }
}*/
