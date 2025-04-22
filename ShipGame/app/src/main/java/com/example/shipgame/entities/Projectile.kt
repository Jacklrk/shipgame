package com.example.shipgame.entities

data class Projectile(

    var x: Float,
    var y: Float,
    val speed: Float,
    val target: Entity, // Referencia al jugador o jugadores
    val disparadoPorJugador: Boolean,
    var active: Boolean = true
)