package com.example.tp5_hiking.models

import org.ktorm.entity.Entity

interface PathHike : Entity<PathHike> {
    companion object : Entity.Factory<PathHike>()

    var hike: Hike
    var position: Position
}
